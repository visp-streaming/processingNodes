package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.peerJ.Availability;
import ac.at.tuwien.infosys.visp.common.peerJ.Warning;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MonitorAvailability {

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(MonitorAvailability.class);

    public Message process(Message message) {

        Message msg = new Message("empty", null);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Availability availability = null;
        try {
            availability = mapper.readValue(message.getPayload(), Availability.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (availability.getAvailability().equals("DEFECT")) {

            try {
                msg = new Message("warning", ow.writeValueAsString(new Warning("machine down", availability.getTimestamp(), availability.getAssetID(), "availability")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }
        }
        return msg;
    }
}
