package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.cloud;

import ac.at.tuwien.infosys.visp.common.cloud.Location;
import ac.at.tuwien.infosys.visp.common.cloud.Report;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.ForwardController;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MonitorController extends GeneralController {

    @Value("${wait.monitor}")
    private Integer wait;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);


    public Message process(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        if (msgutil.getHeader(message).equals("initial")) {
            ObjectMapper mapper = new ObjectMapper();
            Location location = null;
            try {
                location = mapper.readValue(message.getBody(), Location.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }

            if (location.getLatitude().equals("start")) {
                startLog(location);
                return msgutil.createEmptyMessage();
            }

            if (location.getLatitude().equals("stop")) {
                stopLog(location);
                return msgutil.createEmptyMessage();
            }
        }

        if (msgutil.getHeader(message).equals("report")) {
            ObjectMapper mapper = new ObjectMapper();
            Report report = null;
            try {
                report = mapper.readValue(message.getBody(), Report.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }

            reportLog(report);
        }
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        return msgutil.createEmptyMessage();
    }


    public void startLog(Location location) {
        HashOperations<String, String, String> ops = this.template.opsForHash();

        String key = location.getTaxiId();

        ops.put(key, "start", new DateTime().toString());
        LOG.info("logged start of taxiId: " + location.getTaxiId());

    }

    public void stopLog(Location location) {
        String key = location.getTaxiId();
        HashOperations<String, String, String> ops = this.template.opsForHash();

        DateTime finish = new DateTime();

        if (this.template.hasKey(key)) {
            ops.put(key, "stop", finish.toString());
        }

        DateTime start = new DateTime(ops.get(key, "start"));

        Seconds duration = Seconds.secondsBetween(start, finish);

        ops.put(key, "logRide" , duration.toString());
        LOG.info("logged stop of taxiId: " + location.getTaxiId() + " with duration " + duration.toString());
    }


    public void reportLog(Report report) {
        String key = report.getTaxiId();
        HashOperations<String, String, String> ops = this.template.opsForHash();

        DateTime finish = new DateTime();

        if (this.template.hasKey(key)) {
            ops.put(key, "report", finish.toString());
        }

        DateTime stop = new DateTime(ops.get(key, "stop"));

        Seconds duration = Seconds.secondsBetween(stop, finish);

        ops.put(key, "logReport", duration.toString());
        LOG.info("logged report of taxiId: " + report.getTaxiId() + " with duration " + duration.toString());
    }
}
