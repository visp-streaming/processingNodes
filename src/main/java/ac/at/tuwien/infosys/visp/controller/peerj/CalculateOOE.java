package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.peerJ.OOE;
import entities.peerJ.OOEAvailability;
import entities.peerJ.OOEPerformance;
import entities.peerJ.OOEQuality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class CalculateOOE {

    @Autowired
    ErrorHandler error;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculateOOE.class);

    public Message process(Message message) {

        HashOperations<String, String, String> ops = this.template.opsForHash();
        Message msg = new Message("empty", null);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String key = "";
        String timestamp = "";
        String assetid = "";

        if (message.getHeader().equals("ooeavailability")) {
            OOEAvailability ooeAvailability = null;
            try {
                ooeAvailability = mapper.readValue(message.getPayload(), OOEAvailability.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_ooe_" + ooeAvailability.getAssetID() + "_" + ooeAvailability.getTimeStamp();
            timestamp = ooeAvailability.getTimeStamp();
            assetid = ooeAvailability.getAssetID();
            ops.put(key, "availablity", ooeAvailability.getAvailability());
        }

        if (message.getHeader().equals("ooeperformance")) {
            OOEPerformance ooePerformance = null;
            try {
                ooePerformance = mapper.readValue(message.getPayload(), OOEPerformance.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_ooe_" + ooePerformance.getAssetID() + "_" + ooePerformance.getTimeStamp();
            timestamp = ooePerformance.getTimeStamp();
            assetid = ooePerformance.getAssetID();
            ops.put(key, "performance", ooePerformance.getPerformance());
        }

        if (message.getHeader().equals("ooequality")) {
            OOEQuality ooeQuality = null;
            try {
                ooeQuality = mapper.readValue(message.getPayload(), OOEQuality.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_ooe_" + ooeQuality.getAssetID() + "_" + ooeQuality.getTimeStamp();
            timestamp = ooeQuality.getTimeStamp();
            assetid = ooeQuality.getAssetID();
            ops.put(key, "quality", ooeQuality.getQuality());
        }


        if (ops.hasKey("key", "availablity") && ops.hasKey("key", "performance") && ops.hasKey("key", "quality")) {

            List<String> values = ops.multiGet(key, Arrays.asList("availability", "performance", "quality"));
            ops.delete(key, Arrays.asList("availability", "performance", "quality"));
            Double ooe = Double.parseDouble(values.get(0)) * Double.parseDouble(values.get(2)) * Double.parseDouble(values.get(3));

            try {
                msg = new Message("ooe", ow.writeValueAsString(new OOE(assetid, timestamp, ooe.toString())));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }

            return msg;
        }


        return msg;

    }

}
