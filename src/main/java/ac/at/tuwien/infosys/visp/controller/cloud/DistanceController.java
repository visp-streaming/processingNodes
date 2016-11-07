package ac.at.tuwien.infosys.visp.controller.cloud;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.cloud.Distance;
import entities.cloud.Location;
import entities.cloud.Locations;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DistanceController {


    @Value("${wait.distance}")
    private Integer wait;

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(DistanceController.class);


    public Message calculateDistance(Message message) {
        LOG.info("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Locations locations = null;
        try {
            locations = mapper.readValue(message.getPayload(), Locations.class);
        } catch (IOException e) {
            error.send(e.getMessage());        }

        float overallDistance = 0F;

        Location lastLocation = null;

        for (Location location : locations.getLocations()) {
            if (lastLocation != null) {
                overallDistance+=distance(Float.valueOf(lastLocation.getLatitude()), Float.valueOf(lastLocation.getLongitude()), Float.valueOf(location.getLatitude()), Float.valueOf(location.getLongitude()));
            }
            lastLocation = location;
        }

        Distance distance = new Distance(locations.getLocations().get(0).getTaxiId(), String.valueOf(overallDistance));

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("distance", ow.writeValueAsString(distance));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }


        LOG.trace("Calculated distance for : " + locations.getLocations().get(0).getTaxiId() + " with distance of" + overallDistance);

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }


        return msg;
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
