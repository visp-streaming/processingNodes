package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver;


import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.WaitController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.cloud.*;
import org.springframework.amqp.core.Message;
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

        switch (msgutil.getHeader(message)) {
            case "initial" :
                switch (role) {
                    case "speed":
                        sender.send(speedCalculationController.process(message));
                        break;
                    case "aggregate":
                        sender.send(aggregateRideController.process(message));
                        break;
                    case "monitor":
                        monitorController.process(message);
                        break;
                }
                break;
            case "speed" : sender.send(averageSpeedCalculationController.process(message)); break;
            case "avgSpeed" : sender.send(reportController.process(message)); break;
            case "aggregation" : sender.send(distanceController.process(message)); break;
            case "distance" : sender.send(reportController.process(message)); break;
            case "report" : monitorController.process(message); break;
            case "wait" : sender.send(waitController.process(message)); break;
            case "log" : logController.process(message); break;
            default : break;
        }

    }
}






