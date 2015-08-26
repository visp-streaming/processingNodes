package ac.at.tuwien.infosys.visp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Location;
import entities.Message;
import entities.Speed;
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
public class SpeedCalculationController {

    @Autowired
    private StringRedisTemplate template;

    private String key;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    @RequestMapping(value = "/calculateSpeed", method = RequestMethod.POST)
    public Message forwardMessage(@RequestBody Message message) {
        LOG.trace("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Location location = null;
        try {
            location = mapper.readValue(message.getPayload(), Location.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        key = "speed" + location.getTaxiId();

        HashOperations<String, String, String> ops = this.template.opsForHash();

        Speed speed = calculateSpeed(location, ops);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("speed", ow.writeValueAsString(speed));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        LOG.info("Forwarded message with id: " + message.getId() + " with speed of " + speed.getSpeed() + " for taxi " + speed.getTaxiId());
        return msg;
    }

    private Speed calculateSpeed(Location location, HashOperations<String, String, String> ops) {
        Speed speed = new Speed();
        speed.setTaxiId(location.getTaxiId());

        if (location.getLatitude().equals("start")) {
            ops.put(key, "latitude", location.getLatitude());
            ops.put(key, "longitude", location.getLongitude());
            ops.put(key, "timestamp", location.getTime());
            speed.setSpeed("0");
        } else {
            if (!location.getLatitude().equals("stop")) {
                String pastLatitude = ops.get(key, "latitude");
                String pastLongitude = ops.get(key, "longitude");
                String pastTime = ops.get(key, "timestamp");

                if (pastLatitude.equals("start")) {
                    ops.put(key, "latitude", location.getLatitude());
                    ops.put(key, "longitude", location.getLongitude());
                    ops.put(key, "timestamp", location.getTime());
                    speed.setSpeed("0");
                } else {
                    Float distance = distance(Float.parseFloat(pastLatitude), Float.parseFloat(pastLongitude), Float.parseFloat(location.getLatitude()), Float.parseFloat(location.getLongitude()));
                    Long timediff = Long.parseLong(location.getTime())-Long.parseLong(pastTime);

                    Double currentSpeed = 0.0;

                    if (timediff != 0) {
                        timediff = timediff / 1000; //convert to seconds
                        currentSpeed = distance.doubleValue()/timediff.doubleValue() * 3.6;
                    }

                    //replace old position with new one
                    ops.put(key, "latitude", location.getLatitude());
                    ops.put(key, "longitude", location.getLongitude());
                    ops.put(key, "timestamp", location.getTime());
                    speed.setSpeed(currentSpeed.toString());
                }
            } else {
                speed.setSpeed("-1");
            }
        }
        return speed;
    }


    /*
    returns distance in meters
     */
    public float distance(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
