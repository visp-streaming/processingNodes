package ac.at.tuwien.infosys.visp.processingNode.controller.cloud;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.processingNode.controller.ForwardController;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.cloud.Distance;
import ac.at.tuwien.infosys.visp.common.cloud.Report;
import ac.at.tuwien.infosys.visp.common.cloud.Speed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReportController {


    @Value("${wait.report}")
    private Integer wait;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    ErrorHandler error;

    private String key;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    public Message report(Message message) {
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
                error.send(e.getMessage());
            }

            key = speed.getTaxiId();

            if (this.template.hasKey(key)) {
               String distance = ops.get(key, "distance");
                Report report = new Report(speed.getTaxiId(), speed.getSpeed(), distance);

                try {
                    msg = new Message("report", ow.writeValueAsString(report));
                    LOG.info("Forwarded report for taxi : " + report.getTaxiId() + " with speed of " + report.getAverageSpeed() + " and distance " + report.getDistance());
                    return msg;
                } catch (JsonProcessingException e) {
                    error.send(e.getMessage());
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
                error.send(e.getMessage());
            }

            key = distance.getTaxiId();

            if (this.template.hasKey(key)) {
                String speed = ops.get(key, "speed");
                Report report = new Report(distance.getTaxiId(), speed, distance.getDistance());

                try {
                    msg = new Message("report", ow.writeValueAsString(report));
                    LOG.trace("Forwarded report for taxi : " + report.getTaxiId() + " with speed of " + report.getAverageSpeed() + " and distance " + report.getDistance());
                    return msg;
                } catch (JsonProcessingException e) {
                    error.send(e.getMessage());
                }
            } else {
                ops.put(key, "distance", distance.getDistance());
            }
            return msg;
        }

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }


        return msg;
    }


}
