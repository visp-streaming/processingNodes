package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.processingNode.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.controller.WaitController;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.controller.cloud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class Receiver_Cloud {

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

    @Autowired
    DurationHandler duration;

    @Value("${role}")
    private String role;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver_Cloud.class);


//    @RabbitListener(queues = {"#{'${incomingqueues}'.split('_')}"})
    public void assign(Message message) throws InterruptedException {

        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
            return;
        }

        switch (message.getHeader()) {
            case "initial" :
                switch (role) {
                    case "speed":
                        sender.send(speedCalculationController.speedCalculation(message));
                        break;
                    case "aggregate":
                        sender.send(aggregateRideController.aggregateMessages(message));
                        break;
                    case "monitor":
                        monitorController.trackMessage(message);
                        break;
                }
                break;
            case "speed" : sender.send(averageSpeedCalculationController.speedcalculation(message)); break;
            case "avgSpeed" : sender.send(reportController.report(message)); break;
            case "aggregation" : sender.send(distanceController.calculateDistance(message)); break;
            case "distance" : sender.send(reportController.report(message)); break;
            case "report" : monitorController.trackMessage(message); break;
            case "wait" : sender.send(waitController.forwardMessagewithWait(message)); break;
            case "log" : logController.logMessage(message); break;
            default : break;
        }



        if ((int) (Math.random() * 10) == 1) {
            duration.send(message.getProcessingDuration());
        }
    }
}






