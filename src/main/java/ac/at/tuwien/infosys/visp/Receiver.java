package ac.at.tuwien.infosys.visp;


import ac.at.tuwien.infosys.visp.controller.*;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    @RabbitListener(queues = "helloWorld")
    public void assign(Message message) throws InterruptedException {

        if (message.getHeader().equals("initial")) {
            sender.send(speedCalculationController.speedCalculation(message));

            sender.send(aggregateRideController.aggregateMessages(message));

            monitorController.trackMessage(message);
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
            logController.forwardMessage(message);

            monitorController.trackMessage(message);

        }
    }


    }






