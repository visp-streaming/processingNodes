package ac.at.tuwien.infosys.visp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Distance;
import entities.Location;
import entities.Locations;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DistanceController {

    private static final Logger LOG = LoggerFactory.getLogger(DistanceController.class);


    @RequestMapping(value = "/distance", method = RequestMethod.POST)
    public Message forwardMessage(@RequestBody Message message) {
        LOG.info("Received message with id: " + message.getId());

        ObjectMapper mapper = new ObjectMapper();
        Locations locations = null;
        try {
            locations = mapper.readValue(message.getPayload(), Locations.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        Message msg = null;
        try {
            msg = new Message("distance", ow.writeValueAsString(overallDistance));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        LOG.info("Calculated distance for : " + message.getId());

        return message;
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
