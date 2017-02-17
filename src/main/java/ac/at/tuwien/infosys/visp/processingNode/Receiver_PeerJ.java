package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.processingNode.controller.ForwardController;
import ac.at.tuwien.infosys.visp.processingNode.controller.LogController;
import ac.at.tuwien.infosys.visp.processingNode.controller.WaitController;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.controller.peerj.*;
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
public class Receiver_PeerJ {

    @Autowired
    Sender sender;

    @Autowired
    DurationHandler duration;

    @Autowired
    ErrorHandler error;

    @Value("${role}")
    private String role;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver_PeerJ.class);

    @Autowired
    private DistributeData distributeData;

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


    //@RabbitListener(queues = {"#{'${incomingqueues}'.split('_')}"})
    public void assign(Message message) throws InterruptedException {

        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
            return;
        }

        switch (message.getHeader().toLowerCase()) {
            case "wait" : sender.send(waitController.forwardMessagewithWait(message)); break;
            case "forward" : sender.send(forwardController.forwardMessage(message)); break;
            case "log" : logController.logMessage(message); break;
            case "initialmachinedata" : sender.send(distributeData.process(message)); break;
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
            default:  error.send("Message could not be distributed" + message.toString());
        }



        if ((int) (Math.random() * 10) == 1) {
            duration.send(message.getProcessingDuration());
        }
    }
}






