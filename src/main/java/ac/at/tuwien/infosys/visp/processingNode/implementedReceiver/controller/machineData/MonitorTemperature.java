package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.peerJ.Temperature;
import ac.at.tuwien.infosys.visp.common.peerJ.Warning;
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
public class MonitorTemperature extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorTemperature.class);

    public Message process(Message message) {

        Message msg = msgutil.createEmptyMessage();

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Temperature temperature = null;
        try {
            temperature = mapper.readValue(message.getBody(), Temperature.class);
        } catch (IOException e) {
            error.send(e.getMessage());
            LOG.error(e.getMessage());
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        if (temperature.getTemperature() > 100) {

            try {
                msg = msgutil.createMessage("warning", ow.writeValueAsBytes(new Warning("some like it hot", temperature.getTimestamp(), temperature.getAssetID(), "temperature (" + temperature + ")")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
                LOG.error(e.getMessage());
            }
        }
        return msg;
    }
}
