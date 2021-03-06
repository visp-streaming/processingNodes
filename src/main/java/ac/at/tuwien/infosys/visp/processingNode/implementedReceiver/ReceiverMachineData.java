package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver;


import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.ForwardController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.WaitController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData.*;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "visp.receiver", havingValue = "machinedata")
public class ReceiverMachineData extends Receiver {

    @Autowired
    private ParseAndDistributeData parseAndDistributeData;

    @Autowired
    private CalculateAvailability calculateAvailability;

    @Autowired
    private CalculatePerformance calculatePerformance;

    @Autowired
    private CalculateQuality calculateQuality;

    @Autowired
    private CalculateOEE calculateOEE;

    @Autowired
    private GenerateReport generateReport;

    @Autowired
    private InformUser informUser;

    @Autowired
    private MonitorAvailability monitorAvailability;

    @Autowired
    private MonitorTemperature monitorTemperature;

    @Autowired
    private ForwardController forwardController;

    @Autowired
    private WaitController waitController;

    @Autowired
    private LogController logController;

    public void assign(Message message) throws InterruptedException {

        switch (msgutil.getHeader(message).toLowerCase()) {
            case "wait" : sender.send(waitController.process(message)); break;
            case "forward" : sender.send(forwardController.process(message)); break;
            case "log" : logController.process(message); break;
            case "initialmachinedata" : sender.send(parseAndDistributeData.process(message)); break;
            case "distributedata" :
                switch (role) {
                case "calculateavailability": sender.send(calculateAvailability.process(message)); break;
                case "calculateperformance": sender.send(calculatePerformance.process(message)); break;
                case "calculatequality": sender.send(calculateQuality.process(message)); break;
            } break;
            case "oeeavailability" :  sender.send(calculateOEE.process(message)); break;
            case "oeeperformance" :  sender.send(calculateOEE.process(message)); break;
            case "oeequality" :  sender.send(calculateOEE.process(message)); break;
            case "oee" :  sender.send(generateReport.process(message)); break;
            case "warning" :  sender.send(informUser.process(message)); break;
            case "temperature" : sender.send(monitorTemperature.process(message)); break;
            case "availability" : sender.send(monitorAvailability.process(message)); break;
            default:  errorHandler.send("Message could not be distributed" + message.toString());
        }
    }
}






