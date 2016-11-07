package ac.at.tuwien.infosys.visp;


import ac.at.tuwien.infosys.visp.controller.LogController;
import ac.at.tuwien.infosys.visp.controller.WaitController;
import ac.at.tuwien.infosys.visp.monitor.ProcessingNodeMonitor;
import ac.at.tuwien.infosys.visp.topology.generic.ApplicationMonitorOperator;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Value("${role}")
    private String role;
    
    public static final String APPNAME = "default";
	
    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private ProcessingNodeMonitor monitor;

    @RabbitListener(queues = { "#{'${incomingqueues}'.split('_')}" } )
    public void assign(Message message) throws InterruptedException {

        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
           return;
        }


        switch(role.toLowerCase()){
        case "source":
        	LOG.info("role setted as source! Message discarded");
        	break;
        case "step1":
        	LOG.info("step1 has received a message");
        	sender.send(waitController.waitAndForwardByRole(role, message));
        	break;
        case "step2":
        	LOG.info("step1 hsa received a message");
        	sender.send(waitController.waitAndForwardByRole(role, message));
        	break;
        case "consumer":
        	LOG.info("consumer has received a message");
        	sender.send(waitController.waitAndForwardByRole(role, message));
        	break;
        case "monitor":
        	appMonitor.handleMessage(APPNAME, message);
        	break;
        }
                 
    	monitor.notifyProcessedMessage(role);
    	
    }


}
