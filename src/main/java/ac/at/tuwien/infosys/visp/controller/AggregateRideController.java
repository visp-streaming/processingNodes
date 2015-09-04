package ac.at.tuwien.infosys.visp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Location;
import entities.Locations;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AggregateRideController {

    private static final Logger LOG = LoggerFactory.getLogger(AggregateRideController.class);

    @Autowired
    private StringRedisTemplate template;

    @RequestMapping(value = "/aggregate", method = RequestMethod.POST)
    public Message aggregateMessages(@RequestBody Message message) {
        LOG.trace("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Location location = null;
        try {
            location = mapper.readValue(message.getPayload(), Location.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String key = "aggregation" + location.getTaxiId();

        ListOperations<String, String> ops = this.template.opsForList();

        if ((!location.getLongitude().equals("start")) && (!location.getLatitude().equals("stop"))) {
            ops.rightPush(key, message.getPayload());
            LOG.trace("Stored message with id: " + message.getId());
        }


        if (location.getLatitude().equals("stop")) {
            List<Location> locations = new ArrayList<>();

            while (ops.size(key) > 0) {
                String singleLocation = ops.leftPop(key);
                try {
                    locations.add(mapper.readValue(singleLocation, Location.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Locations locationList = new Locations();
            locationList.setLocations(locations);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            Message msg = new Message("empty", null);
            try {
                msg = new Message("aggregation", ow.writeValueAsString(locationList));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            LOG.trace("Forwarded new trip for taxi with id : " + location.getTaxiId());
            return msg;
        }

        return new Message("empty", null);
    }
}
