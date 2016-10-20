package ac.at.tuwien.infosys.visp.controller.cloud;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import ac.at.tuwien.infosys.visp.controller.ForwardController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.cloud.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AverageSpeedCalculationController {


    @Value("${wait.averagespeed}")
    private Integer wait;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    ErrorHandler error;


    private String key;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    public Message speedcalculation(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Speed speed = null;
        try {
            speed = mapper.readValue(message.getPayload(), Speed.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        key = "averageSpeed" + speed.getTaxiId();

        ValueOperations<String, String> ops = this.template.opsForValue();

        Speed resultSpeed = new Speed();
        resultSpeed.setTaxiId(speed.getTaxiId());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);

        ops.setIfAbsent(key, "0.0");

        if (speed.getSpeed().equals("-1")) {
            try {
                Speed avgSpeed = new Speed(speed.getTaxiId(), ops.get(key));
                msg = new Message("avgSpeed", ow.writeValueAsString(avgSpeed));
                LOG.trace("Forwarded message with id: " + message.getId() + " with AVG Speed of " + ow.writeValueAsString(ops.get(key)) + " for taxi " + speed.getTaxiId());
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }
        } else {
            String lastSpeed = ops.get(key);
            String newSpeed = String.valueOf((Double.valueOf(lastSpeed) + Double.valueOf(speed.getSpeed())) / 2);
            ops.set(key, newSpeed);
        }

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }


        return msg;
    }

}
