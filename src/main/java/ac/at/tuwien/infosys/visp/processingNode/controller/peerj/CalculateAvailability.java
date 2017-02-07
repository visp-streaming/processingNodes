package ac.at.tuwien.infosys.visp.processingNode.controller.peerj;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.peerJ.MachineData;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculateAvailability {

    @Autowired
    ErrorHandler error;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculateAvailability.class);


    public Message process(Message message) {

        //Production cycle starts with the begin of the evaluation and ends after the evaluation

        ObjectMapper mapper = new ObjectMapper();
        MachineData machineData = null;
        try {
            machineData = mapper.readValue(message.getPayload(), MachineData.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        String keyOperatingTime = "peerj_operatingTime_" + machineData.getAssetID();
        String keyScheduledTime = "peerj_scheduledTime_" + machineData.getAssetID();
        String keyTotalTime = "peerj_TotalTime_" + machineData.getAssetID();


        ops.setIfAbsent(keyOperatingTime, "0");
        ops.setIfAbsent(keyScheduledTime, "0");
        ops.setIfAbsent(keyTotalTime, "0");

        ops.increment(keyTotalTime, 1);

        switch (machineData.getActive()) {
            case "ACTIVE" : ops.increment(keyOperatingTime, 1L); ops.increment(keyScheduledTime, 1); break;
            case "PLANNEDDOWNTIME" : ops.increment(keyScheduledTime, 1); break;
            case "DEFECT" : break;
            default: break;
        }

        Double availability =  Double.parseDouble(ops.get(keyOperatingTime)) / Double.parseDouble(ops.get(keyScheduledTime));

        OEEAvailability oeeavailability = new OEEAvailability(machineData.getAssetID(), machineData.getTimestamp(), availability.toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("oeeavailability", ow.writeValueAsString(oeeavailability));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }
}
