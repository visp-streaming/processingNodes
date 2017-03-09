package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.processingNode.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.controller.WaitController;
import ac.at.tuwien.infosys.visp.processingNode.monitor.ProcessingNodeMonitor;
import ac.at.tuwien.infosys.visp.processingNode.topology.generic.ApplicationMonitorOperator;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.watcher.TopologyUpdateWatchService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class Receiver {

    @Autowired
    Sender sender;

    @Autowired
    LogController logController;

    @Autowired
    WaitController waitController;

    @Autowired
    ApplicationMonitorOperator appMonitor;

    ExecutorService executorService = Executors.newCachedThreadPool();

    ReentrantLock topologyUpdateLock = new ReentrantLock();

    Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    Map<String, DefaultConsumer> registeredConsumers = new ConcurrentHashMap<>();

    Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    Map<String, String> tagMap = new ConcurrentHashMap<>();

    @Value("${role}")
    private String role;

    @Value("${incomingqueues}")
    private String incomingQueues;

    @Autowired
    TopologyUpdateWatchService topologyUpdateWatchService;

    public static final String APPNAME = "default";

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private ProcessingNodeMonitor monitor;

    public void assign(Message message) throws InterruptedException {
        // if handle is set to false, do not actually handle the message
//        Path topologyUpdate = Paths.get("/root/topologyUpdate");
//        if (Files.exists(topologyUpdate)) {
//            try {
//                handleTopologyUpdate(topologyUpdate);
//            } catch (IOException e) {
//                LOG.error(e.getLocalizedMessage());
//            }
//        }
        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
            return;
        }


        switch (role.toLowerCase()) {
            case "source":
                LOG.info("role setted as source! Message discarded");
                break;
            case "step1":
                LOG.info("step1 has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
                break;
            case "step2":
                LOG.info("step2 has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
                break;

            case "monitor":
                appMonitor.handleMessage(APPNAME, message);
                break;
            default:
                LOG.info(role + " has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
        }

        monitor.notifyProcessedMessage(role);

    }

    public void handleTopologyUpdate(Path topologyUpdate) throws IOException {
        try {
            LOG.info("Acquiring lock prior to handling topology update");
            topologyUpdateLock.lock();
            if (!topologyUpdate.toFile().exists()) {
                LOG.error("topology file does not exist at " + topologyUpdate.toFile().toString());
                return;
            }
            LOG.info("Reading file " + topologyUpdate.toFile().getAbsolutePath());
            try (BufferedReader br = new BufferedReader(new FileReader(topologyUpdate.toFile()))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                List<String> lines = new ArrayList<>();
                lines.add(line);

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                    if (line != null && !line.equals("")) {
                        lines.add(line.trim());
                    }
                }
                String everything = sb.toString();
                LOG.info(everything.trim());
                applyTopologyChanges(lines);
            }
            LOG.info("Removing file " + topologyUpdate.toFile().getAbsolutePath());
            topologyUpdate.toFile().delete();
        } finally {
            LOG.info("Releasing lock after topology update");
            topologyUpdateLock.unlock();
        }
    }

    private void applyTopologyChanges(List<String> lines) {
        for (String line : lines) {
            LOG.info("Processing line " + line);
            try {
                String[] splitMessage = line.split(" ");
                switch (splitMessage[0]) {
                    case "ADD":
                        startListeningToQueue(new QueueDefinition(splitMessage[1]), "visp", "visp");
                        break;
                    case "REMOVE":
                        stopListeningToQueue(new QueueDefinition(splitMessage[1]), "visp", "visp");
                        break;
                    default:
                        throw new RuntimeException("Unable to process line from topology update file: " + splitMessage);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }

    public void processMessage(Message message) {
        LOG.info("Processing message " + message.toString());
        try {
            assign(message);
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    @PostConstruct
    public void startListening() {
        LOG.info("START Listening on incoming queue source>step1");
        try {

            listen(incomingQueues, "visp", "visp");
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (TimeoutException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    public void listen(String incomingQueuesString, String username, String password) throws IOException, TimeoutException {
        String infrastructureHost = "", senderOperator = "", receiverOperator = "";

        IncomingQueueExtractor incomingQueueExtractor = new IncomingQueueExtractor(
                incomingQueuesString, infrastructureHost, senderOperator, receiverOperator);
        List<QueueDefinition> queueDefinitions = incomingQueueExtractor.getQueueDefinitions();

        for (QueueDefinition q : queueDefinitions) {
            startListeningToQueue(q, username, password);
        }
        //executorService.shutdown();

    }

    private void stopListeningToQueue(QueueDefinition queue, String username, String password) throws IOException, TimeoutException {
        LOG.info("stop listening to queue: " + queue.toString());
        String tagToStop = tagMap.get(queue.toString());
        if (channelMap.containsKey(queue.toString())) {
            Channel channel = channelMap.get(queue.toString());
            channel.basicCancel(tagToStop);
            LOG.info("Cancelled consumption for consumer tag [" + tagToStop + "]");
            channel.close();
            channelMap.remove(queue.toString());
        } else {
            LOG.error("Not able to cancel consumption for consumer tag [" + tagToStop + "]");
        }
    }

    private void startListeningToQueue(QueueDefinition queue, String username, String password) throws IOException, TimeoutException {
        Connection connection;
        // make sure to only use one connection per infrastructure host
        if (connectionMap.containsKey(queue.infrastructureHost)) {
            LOG.info("Reusing connection to host " + queue.infrastructureHost);
            connection = connectionMap.get(queue.infrastructureHost);
        } else {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(queue.infrastructureHost);
            factory.setUsername(username);
            factory.setPassword(password);
            LOG.info("Creating new connection to host " + queue.infrastructureHost);
            connection = factory.newConnection();
            connectionMap.put(queue.infrastructureHost, connection);
        }
        Channel channel = connection.createChannel();
        channelMap.put(queue.toString(), channel);
        String queueName = queue.toString();
        LOG.info("Start listening to queue " + queue.toString());

        QueueingConsumer consumer = new QueueingConsumer(channel);
        String tag = channel.basicConsume(queueName, true, consumer);
        tagMap.put(queueName, tag);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
                        ObjectInput in = new ObjectInputStream(bis);
                        try {
                            Message msg = (Message) in.readObject();
                            LOG.info(consumer.getConsumerTag() + " is doing something...");
                            Receiver.this.processMessage(msg);
                        } catch (ClassNotFoundException e) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    } catch (Exception ex) {
                        LOG.error(ex.getLocalizedMessage());
                    }
                }
            }
        });

    }

    private class QueueDefinition {
        public String infrastructureHost;
        public String senderOperator;
        public String receiverOperator;

        public QueueDefinition(String infrastructureHost, String senderOperator, String receiverOperator) {
            this.infrastructureHost = infrastructureHost;
            this.senderOperator = senderOperator;
            this.receiverOperator = receiverOperator;
        }

        public QueueDefinition(String identifier) {
            String[] hostAndRemainder = identifier.split("/");
            infrastructureHost = hostAndRemainder[0];
            String[] senderAndReceiver = hostAndRemainder[1].split(">");
            senderOperator = senderAndReceiver[0];
            receiverOperator = senderAndReceiver[1];
            this.infrastructureHost = infrastructureHost;
            this.receiverOperator = receiverOperator;
            this.senderOperator = senderOperator;
        }

        @Override
        public String toString() {
            return infrastructureHost + "/" + senderOperator + ">" + receiverOperator;
        }


    }

    private class IncomingQueueExtractor {
        private String incomingQueuesString;
        private String infrastructureHost;
        private String senderOperator;
        private String receiverOperator;


        public IncomingQueueExtractor(String incomingQueuesString, String infrastructureHost, String senderOperator, String receiverOperator) {
            this.incomingQueuesString = incomingQueuesString;
            this.infrastructureHost = infrastructureHost;
            this.senderOperator = senderOperator;
            this.receiverOperator = receiverOperator;
        }

        public List<QueueDefinition> getQueueDefinitions() {
            try {
                List<QueueDefinition> returnList = new ArrayList<>();
                if(incomingQueuesString == null || incomingQueuesString.equals("")) {
                    return returnList; // no incoming queues (i.e., source)
                }
                String[] incomingQueues = incomingQueuesString.split("_");

                for (String queue : incomingQueues) {
                    returnList.add(new QueueDefinition(queue));
                }

                return returnList;
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse list of incoming queues", e);
            }
        }
    }
}
