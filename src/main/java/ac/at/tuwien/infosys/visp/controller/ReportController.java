package ac.at.tuwien.infosys.visp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ReportController {

    @Autowired
    private StringRedisTemplate template;

    private String key;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public Message forwardMessage(@RequestBody Message message) {
        LOG.trace("Received message with id: " + message.getId());

        HashOperations<String, String, String> ops = this.template.opsForHash();
        Message msg = new Message("empty", null);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        if (message.getHeader().equals("avgSpeed")) {
            Speed speed = null;
            try {
                speed = mapper.readValue(message.getPayload(), Speed.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            key = "report" + speed.getTaxiId();

            if (this.template.hasKey(key)) {
               String distance = ops.get(key, "distance");
                Report report = new Report(speed.getTaxiId(), speed.getSpeed(), distance);

                try {
                    msg = new Message("report", ow.writeValueAsString(report));
                    LOG.info("Forwarded report for taxi : " + report.getTaxiId() + " with speed of " + report.getAverageSpeed() + " and distance " + report.getDistance());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                ops.put(key, "speed", speed.getSpeed());
            }
        }


        if (message.getHeader().equals("distance")) {
            Distance distance = null;
            try {
                distance = mapper.readValue(message.getPayload(), Distance.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            key = "report" + distance.getTaxiId();

            if (this.template.hasKey(key)) {
                String speed = ops.get(key, "speed");
                Report report = new Report(distance.getTaxiId(), speed, distance.getDistance());

                try {
                    msg = new Message("report", ow.writeValueAsString(report));
                    LOG.trace("Forwarded report for taxi : " + report.getTaxiId() + " with speed of " + report.getAverageSpeed() + " and distance " + report.getDistance());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                ops.put(key, "distance", distance.getDistance());
            }
            return msg;
        }
        return msg;
    }


}
