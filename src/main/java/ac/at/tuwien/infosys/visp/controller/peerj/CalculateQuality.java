package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.peerJ.MachineData;
import entities.peerJ.OOEQuality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculateQuality {

    @Autowired
    ErrorHandler error;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculateQuality.class);


    public Message process(Message message) {

        ObjectMapper mapper = new ObjectMapper();
        MachineData machineData = null;
        try {
            machineData = mapper.readValue(message.getPayload(), MachineData.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        String keyProducedUnits = "peerj_producedUnits_" + machineData.getAssetID();
        String keyDefectUnits = "peerj_defectUnits_" + machineData.getAssetID();

        ops.setIfAbsent(keyProducedUnits, "0");
        ops.setIfAbsent(keyDefectUnits, "0");

        ops.increment(keyProducedUnits, machineData.getProducedUnits());
        ops.increment(keyDefectUnits, machineData.getDefectiveUnits());

        Double producedUnits = Double.parseDouble(ops.get(keyProducedUnits));
        Double defectUnits = Double.parseDouble(ops.get(keyDefectUnits));

        Double quality = (producedUnits - defectUnits) / producedUnits;

        OOEQuality ooeQuality = new OOEQuality(machineData.getAssetID(), machineData.getTimestamp(), quality.toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("ooequality", ow.writeValueAsString(ooeQuality));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }

}
