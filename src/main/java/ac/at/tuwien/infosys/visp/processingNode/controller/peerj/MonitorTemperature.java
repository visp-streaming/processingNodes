package ac.at.tuwien.infosys.visp.processingNode.controller.peerj;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.peerJ.Temperature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import ac.at.tuwien.infosys.visp.common.peerJ.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MonitorTemperature {

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(MonitorTemperature.class);

    public Message process(Message message) {

        Message msg = new Message("empty", null);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Temperature temperature = null;
        try {
            temperature = mapper.readValue(message.getPayload(), Temperature.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        if (temperature.getTemperature() > 100) {

            try {
                msg = new Message("warning", ow.writeValueAsString(new Warning("some like it hot", temperature.getTimestamp(), temperature.getAssetID(), "temperature (" + temperature + ")")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }
        }
        return msg;
    }
}
