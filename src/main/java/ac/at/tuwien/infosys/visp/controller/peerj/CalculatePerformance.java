package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.DurationHandler;
import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.peerJ.MachineData;
import entities.peerJ.OOEPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculatePerformance {

    @Autowired
    ErrorHandler error;

    @Autowired
    DurationHandler duration;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculatePerformance.class);

    public Message process(Message message) {

        ObjectMapper mapper = new ObjectMapper();
        MachineData machineData = null;
        try {
            machineData = mapper.readValue(message.getPayload(), MachineData.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        String keyOperatingTime = "peerj_operatingTime_" + machineData.getAssetID();
        String keyProducedUnits = "peerj_producedUnits_" + machineData.getAssetID();

        ops.setIfAbsent(keyProducedUnits, "0");
        ops.setIfAbsent(keyOperatingTime, "0");

        Double producedUnits = Double.parseDouble(ops.get(keyProducedUnits));
        Double operatingTime = Double.parseDouble(ops.get(keyOperatingTime));

        Double performance = (producedUnits / machineData.getPlannedProductionTime()) / operatingTime;

        OOEPerformance ooePerformacne = new OOEPerformance(machineData.getAssetID(), machineData.getTimestamp(), performance.toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("ooeperformance", ow.writeValueAsString(ooePerformacne));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }
}
