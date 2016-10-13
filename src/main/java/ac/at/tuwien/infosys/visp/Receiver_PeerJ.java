package ac.at.tuwien.infosys.visp;


import ac.at.tuwien.infosys.visp.controller.peerj.*;
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
public class Receiver_PeerJ {

    @Autowired
    Sender sender;

    @Autowired
    DurationHandler duration;

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
    private CalculateOOE calculateOOE;

    @Autowired
    private GenerateReport generateReport;

    @Autowired
    private InformUser informUser;

    @Autowired
    private MonitorAvailability monitorAvailability;

    @Autowired
    private MonitorTemperature monitorTemperature;



    @RabbitListener(queues = {"#{'${incomingqueues}'.split('_')}"})
    public void assign(Message message) throws InterruptedException {

        Path path = Paths.get("~/killme");

        if (Files.exists(path)) {
            return;
        }

        switch (message.getHeader()) {
            case "initialMachineData" : sender.send(distributeData.process(message)); break;
            case "machineData" :
                switch (role) {
                case "calculateAvailability": sender.send(calculateAvailability.process(message)); break;
                case "calculatePerformance": sender.send(calculatePerformance.process(message)); break;
                case "calculateQuality": sender.send(calculateQuality.process(message)); break;
            } break;
            case "ooeavailability" :  sender.send(calculateOOE.process(message)); break;
            case "ooeperformance" :  sender.send(calculateOOE.process(message)); break;
            case "ooequality" :  sender.send(calculateOOE.process(message)); break;
            case "ooe" :  sender.send(generateReport.process(message)); break;
            case "warning" :  sender.send(informUser.process(message)); break;
            case "temperature" : sender.send(monitorTemperature.process(message)); break;
            case "availability" : sender.send(monitorAvailability.process(message)); break;
            default: break;
        }


        if ((int) (Math.random() * 10) == 1) {
            duration.send(message.getProcessingDuration());
        }
    }
}






