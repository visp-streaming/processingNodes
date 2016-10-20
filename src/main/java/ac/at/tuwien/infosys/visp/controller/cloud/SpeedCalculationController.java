package ac.at.tuwien.infosys.visp.controller.cloud;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import ac.at.tuwien.infosys.visp.controller.ForwardController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.cloud.Location;
import entities.Message;
import entities.cloud.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SpeedCalculationController {

    @Value("${wait.speed}")
    private Integer wait;

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    ErrorHandler error;

    private String key;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    public Message speedCalculation(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Location location = null;
        try {
            location = mapper.readValue(message.getPayload(), Location.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        key = "speed" + location.getTaxiId();

        HashOperations<String, String, String> ops = this.template.opsForHash();

        Speed speed = calculateSpeed(location, ops);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("speed", ow.writeValueAsString(speed));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }


        LOG.trace("Forwarded message with id: " + message.getId() + " with speed of " + speed.getSpeed() + " for taxi " + speed.getTaxiId());

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }
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
            if (location.getLatitude().equals("stop")) {
                speed.setSpeed("-1");
            } else {

                String pastLatitude = ops.get(key, "latitude");
                String pastLongitude = ops.get(key, "longitude");
                String pastTime = ops.get(key, "timestamp");

                //this is some error compensation...
                if (pastLatitude == null) {
                    pastLatitude = "1.0";
                    pastLongitude = "1.0";
                    pastTime = location.getTime();
                }

                if (pastLatitude.equals("start")) {
                    ops.put(key, "latitude", location.getLatitude());
                    ops.put(key, "longitude", location.getLongitude());
                    ops.put(key, "timestamp", location.getTime());
                    speed.setSpeed("0");
                } else {
                    try {
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

                    } catch (Exception ex) {
                        ops.put(key, "1.0", location.getLatitude());
                        ops.put(key, "1.0", location.getLongitude());
                        ops.put(key, "timestamp", location.getTime());
                        speed.setSpeed("0");

                    }
                }
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
