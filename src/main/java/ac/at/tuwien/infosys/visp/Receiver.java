package ac.at.tuwien.infosys.visp;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ac.at.tuwien.infosys.visp.controller.AggregateRideController;
import ac.at.tuwien.infosys.visp.controller.AverageSpeedCalculationController;
import ac.at.tuwien.infosys.visp.controller.DistanceController;
import ac.at.tuwien.infosys.visp.controller.LogController;
import ac.at.tuwien.infosys.visp.controller.MonitorController;
import ac.at.tuwien.infosys.visp.controller.ReportController;
import ac.at.tuwien.infosys.visp.controller.SpeedCalculationController;
import ac.at.tuwien.infosys.visp.controller.WaitController;
import ac.at.tuwien.infosys.visp.monitor.ProcessingNodeMonitor;
import entities.Message;

@Service
public class Receiver {

    @Autowired
    Sender sender;

    @Autowired
    SpeedCalculationController speedCalculationController;

    @Autowired
    AggregateRideController aggregateRideController;

    @Autowired
    MonitorController monitorController;

    @Autowired
    AverageSpeedCalculationController averageSpeedCalculationController;

    @Autowired
    ReportController reportController;

    @Autowired
    DistanceController distanceController;

    @Autowired
    LogController logController;

    @Autowired
    WaitController waitController;


    @Value("${role}")
    private String role;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private ProcessingNodeMonitor monitor;

    @RabbitListener(queues = { "#{'${incomingqueues}'.split('_')}" } )
    public void assign(Message message) throws InterruptedException {

        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
           return;
        }

        if (message.getHeader().equals("initial")) {
            switch(role) {
                case "speed" : sender.send(speedCalculationController.speedCalculation(message)); break;
                case "aggregate" : sender.send(aggregateRideController.aggregateMessages(message)); break;
                case "monitor" : monitorController.trackMessage(message); break;
            }
        }

        if (message.getHeader().equals("speed")) {
            sender.send(averageSpeedCalculationController.speedcalculation(message));
        }

        if (message.getHeader().equals("avgSpeed")) {
            sender.send(reportController.report(message));
        }

        if (message.getHeader().equals("aggregation")) {
            sender.send(distanceController.calculateDistance(message));
        }

        if (message.getHeader().equals("distance")) {
            sender.send(reportController.report(message));
        }

        if (message.getHeader().equals("report")) {
            monitorController.trackMessage(message);
        }

        if (message.getHeader().equals("wait")) {
            sender.send(waitController.forwardMessagewithWait(message));
        }

        if (message.getHeader().equals("log")) {
        	logController.logMessage(message);
        }
        
    	monitor.notifyProcessedMessage(role);
    	
    }


}
