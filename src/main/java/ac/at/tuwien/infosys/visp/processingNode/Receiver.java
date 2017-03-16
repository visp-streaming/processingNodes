package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.monitor.ProcessingNodeMonitor;
import ac.at.tuwien.infosys.visp.processingNode.monitor.ApplicationMonitorOperator;
import ac.at.tuwien.infosys.visp.processingNode.util.IncomingQueueExtractor;
import ac.at.tuwien.infosys.visp.processingNode.util.QueueDefinition;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

@Service
public abstract class Receiver {

    @Autowired
    protected Sender sender;

    @Autowired
    protected ApplicationMonitorOperator appMonitor;

    @Autowired
    private TopologyUpdateWatchService topologyUpdateWatchService;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    private DurationHandler durationHandler;

    @Value("${role}")
    protected String role;

    @Value("${incomingqueues}")
    private String incomingQueues;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private ReentrantLock topologyUpdateLock = new ReentrantLock();

    private Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private Map<String, DefaultConsumer> registeredConsumers = new ConcurrentHashMap<>();

    private Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    private Map<String, String> tagMap = new ConcurrentHashMap<>();

    protected static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    protected ProcessingNodeMonitor monitor;

    public static final String APPNAME = "default";

    public abstract void assign(Message message) throws InterruptedException;

    public void handleTopologyUpdate(Path topologyUpdate) throws IOException {
        try {
            LOG.info("Acquiring lock prior to handling topology update");
            topologyUpdateLock.lock();
            if (!topologyUpdate.toFile().exists()) {
                errorHandler.send("topology file does not exist at " + topologyUpdate.toFile().toString());
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

    @PostConstruct
    public void startListening() {
        LOG.info("START Listening on incoming queue");
        try {
            listen(incomingQueues, "visp", "visp");
        } catch (IOException | TimeoutException e) {
            errorHandler.send(e.getLocalizedMessage());
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
                errorHandler.send(e.getLocalizedMessage());
            }
        }
    }

    public void processMessage(Message message) {
        LOG.info("Processing message " + message.toString());
        try {
            if (Files.exists(Paths.get("~/killme"))) {
                return;
            }

            assign(message);

            monitor.notifyProcessedMessage(role);

            if ((int) (Math.random() * 10) == 1) {
                durationHandler.send(message.getProcessingDuration());
            }
        } catch (InterruptedException e) {
            errorHandler.send(e.getLocalizedMessage());
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
            errorHandler.send("Not able to cancel consumption for consumer tag [" + tagToStop + "]");
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
        executorService.execute(() -> {
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
                        errorHandler.send(e.getLocalizedMessage());
                    }
                } catch (InterruptedException | IOException e) {
                    errorHandler.send(e.getLocalizedMessage());
                }
            }
        });
    }
}
