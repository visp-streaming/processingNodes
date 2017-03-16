package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.peerJ.OEE;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEAvailability;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEPerformance;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEQuality;
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
public class CalculateOEE {

    @Autowired
    ErrorHandler error;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculateOEE.class);

    public Message process(Message message) {

        HashOperations<String, String, String> ops = this.template.opsForHash();
        Message msg = new Message("empty", null);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String key = "";
        String timestamp = "";
        String assetid = "";

        if (message.getHeader().equals("oeeavailability")) {
            OEEAvailability OEEAvailability = null;
            try {
                OEEAvailability = mapper.readValue(message.getPayload(), OEEAvailability.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_oee_" + OEEAvailability.getAssetID() + "_" + OEEAvailability.getTimeStamp();
            timestamp = OEEAvailability.getTimeStamp();
            assetid = OEEAvailability.getAssetID();
            ops.put(key, "availablity", OEEAvailability.getAvailability());
        }

        if (message.getHeader().equals("oeeperformance")) {
            OEEPerformance OEEPerformance = null;
            try {
                OEEPerformance = mapper.readValue(message.getPayload(), OEEPerformance.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_oee_" + OEEPerformance.getAssetID() + "_" + OEEPerformance.getTimeStamp();
            timestamp = OEEPerformance.getTimeStamp();
            assetid = OEEPerformance.getAssetID();
            ops.put(key, "performance", OEEPerformance.getPerformance());
        }

        if (message.getHeader().equals("oeequality")) {
            OEEQuality OEEQuality = null;
            try {
                OEEQuality = mapper.readValue(message.getPayload(), OEEQuality.class);
            } catch (IOException e) {
                error.send(e.getMessage());
            }
            key = "peerj_oee_" + OEEQuality.getAssetID() + "_" + OEEQuality.getTimeStamp();
            timestamp = OEEQuality.getTimeStamp();
            assetid = OEEQuality.getAssetID();
            ops.put(key, "quality", OEEQuality.getQuality());
        }


        if (ops.hasKey(key, "availablity") && ops.hasKey(key, "performance") && ops.hasKey(key, "quality")) {

            List<String> values = ops.multiGet(key, Arrays.asList("availability", "performance", "quality"));

            Double oee;

            try {
                oee = Double.parseDouble(values.get(0)) * Double.parseDouble(values.get(2)) * Double.parseDouble(values.get(3));
            } catch (Exception e) {
                oee = 50.0;
            }

            ops.delete(key, "availability");
            ops.delete(key, "performance");
            ops.delete(key, "quality");

            try {
                msg = new Message("oee", ow.writeValueAsString(new OEE(assetid, timestamp, oee.toString())));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }

            return msg;
        }


        return msg;

    }

}
