package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.cloud;

import ac.at.tuwien.infosys.visp.common.cloud.Location;
import ac.at.tuwien.infosys.visp.common.cloud.Locations;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AggregateRideController extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateRideController.class);

    @Value("${wait.aggregate}")
    private Integer wait;

    @Autowired
    private StringRedisTemplate template;

    public Message process(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        ObjectMapper mapper = new ObjectMapper();
        Location location = null;
        try {
            location = mapper.readValue(message.getBody(), Location.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        String key = "aggregation" + location.getTaxiId();

        ListOperations<String, String> ops = this.template.opsForList();

        if ((!location.getLongitude().equals("start")) && (!location.getLatitude().equals("stop"))) {
            ops.rightPush(key, new String(message.getBody()));
            LOG.trace("Stored message with id: " + message.getMessageProperties().getMessageId());
        }


        if (location.getLatitude().equals("stop")) {
            List<Location> locations = new ArrayList<>();

            while (ops.size(key) > 0) {
                String singleLocation = ops.leftPop(key);
                try {
                    locations.add(mapper.readValue(singleLocation, Location.class));
                } catch (IOException e) {
                    error.send(e.getMessage());
                }
            }

            Locations locationList = new Locations();
            locationList.setLocations(locations);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            Message msg = msgutil.createEmptyMessage();
            try {
                msg = msgutil.createMessage("aggregation", ow.writeValueAsBytes(locationList));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }

            LOG.trace("Forwarded new trip for taxi with id : " + location.getTaxiId());
            return msg;
        }

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        return msgutil.createEmptyMessage();
    }
}
