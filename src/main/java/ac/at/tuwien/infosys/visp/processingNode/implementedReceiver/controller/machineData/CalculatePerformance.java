package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.peerJ.MachineData;
import ac.at.tuwien.infosys.visp.common.peerJ.OEEPerformance;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CalculatePerformance extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(CalculatePerformance.class);

    public Message process(Message message) {

        ObjectMapper mapper = new ObjectMapper();
        MachineData machineData = null;
        try {
            machineData = mapper.readValue(message.getBody(), MachineData.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        //we assumes a fixed reporting cycle
        Double performance = (machineData.getProducedUnits() * machineData.getPlannedProductionTime()) / 20.0;

        OEEPerformance oeePerformance = new OEEPerformance(machineData.getAssetID(), machineData.getTimestamp(), performance.toString());

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = msgutil.createEmptyMessage();
        try {
            msg = msgutil.createMessage("oeeperformance", ow.writeValueAsBytes(oeePerformance));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }
}
