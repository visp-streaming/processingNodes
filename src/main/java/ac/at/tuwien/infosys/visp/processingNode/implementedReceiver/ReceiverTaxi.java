package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver;


import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.WaitController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.cloud.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "visp.receiver", havingValue = "taxi")
public class ReceiverTaxi extends Receiver {

    @Autowired
    private SpeedCalculationController speedCalculationController;

    @Autowired
    private AggregateRideController aggregateRideController;

    @Autowired
    private MonitorController monitorController;

    @Autowired
    private AverageSpeedCalculationController averageSpeedCalculationController;

    @Autowired
    private ReportController reportController;

    @Autowired
    private DistanceController distanceController;

    @Autowired
    private LogController logController;

    @Autowired
    private WaitController waitController;


    public void assign(Message message) throws InterruptedException {

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

    }
}






