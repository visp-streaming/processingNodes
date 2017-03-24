package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.peerJ.MachineData;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEQuality;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculateQuality extends GeneralController {

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(CalculateQuality.class);


    public Message process(Message message) {

        ObjectMapper mapper = new ObjectMapper();
        MachineData machineData = null;
        try {
            machineData = mapper.readValue(message.getBody(), MachineData.class);
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

        OEEQuality OEEQuality = new OEEQuality(machineData.getAssetID(), machineData.getTimestamp(), quality.toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = msgutil.createEmptyMessage();
        try {
            msg = msgutil.createMessage("oeequality", ow.writeValueAsBytes(OEEQuality));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }

}
