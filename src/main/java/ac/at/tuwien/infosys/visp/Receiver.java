package ac.at.tuwien.infosys.visp;


import ac.at.tuwien.infosys.visp.controller.*;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Receiver {

    //TODO fetch incoming queue from environment variables

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

    @Value("${role}")
    private String role;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @RabbitListener(queues = "helloWorld")
    public void assign(Message message) throws InterruptedException {

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
    }


    }






