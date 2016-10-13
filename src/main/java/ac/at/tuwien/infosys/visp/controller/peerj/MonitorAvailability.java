package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.peerJ.Availability;
import entities.peerJ.Status;
import entities.peerJ.Warning;
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

        if (availability.getAvailability().equals(Status.DEFECT)) {

            try {
                msg = new Message("warning", ow.writeValueAsString(new Warning("machine down", availability.getTimestamp(), availability.getAssetID(), "availability")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }
        }
        return msg;
    }
}
