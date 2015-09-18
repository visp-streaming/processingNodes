package ac.at.tuwien.infosys.visp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Location;
import entities.Message;
import entities.Report;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MonitorController {

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);


    public void trackMessage(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        if (message.getHeader().equals("initial")) {
            ObjectMapper mapper = new ObjectMapper();
            Location location = null;
            try {
                location = mapper.readValue(message.getPayload(), Location.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (location.getLatitude().equals("start")) {
                startLog(location);
                return;
            }

            if (location.getLatitude().equals("stop")) {
                stopLog(location);
                return;
            }
        }

        if (message.getHeader().equals("report")) {
            ObjectMapper mapper = new ObjectMapper();
            Report report = null;
            try {
                report = mapper.readValue(message.getPayload(), Report.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            reportLog(report);
        }
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

        if (!this.template.hasKey(key)) {
            ops.put(key, "stop", finish.toString());
        }

        DateTime start = new DateTime(ops.get(key, "start"));

        Seconds duration = Seconds.secondsBetween(start, finish);

        HashOperations<String, String, String> logData = this.template.opsForHash();
        ops.put("logRide", key, duration.toString());
        LOG.info("logged stop of taxiId: " + location.getTaxiId() + " with duration " + duration.toString());
    }


    public void reportLog(Report report) {
        String key = report.getTaxiId();
        HashOperations<String, String, String> ops = this.template.opsForHash();

        DateTime finish = new DateTime();

        if (!this.template.hasKey(key)) {
            ops.put(key, "report", finish.toString());
        }

        DateTime stop = new DateTime(ops.get(key, "stop"));

        Seconds duration = Seconds.secondsBetween(stop, finish);

        HashOperations<String, String, String> logData = this.template.opsForHash();
        ops.put("logReport", key, duration.toString());
        LOG.info("logged report of taxiId: " + report.getTaxiId() + " with duration " + duration.toString());
    }
}
