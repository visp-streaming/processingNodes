package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import entities.Message;
import entities.peerJ.OEE;
import entities.peerJ.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GenerateReport {

    @Autowired
    ErrorHandler error;

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReport.class);


    public Message process(Message message) {

        //Production cycle starts with the begin of the evaluation and ends after the evaluation

        ObjectMapper mapper = new ObjectMapper();
        OEE OEE = null;
        try {
            OEE = mapper.readValue(message.getPayload(), OEE.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        //TODO aggregate OEE value over time and generate a report where machines are grouped by location/type and it shows the difference compared to the last oee value (some trend)

        Message msg = new Message("empty", null);

        if ((int) (Math.random() * 100) == 1) {


            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            try {
                msg = new Message("warning", ow.writeValueAsString(new Warning("fancy report", OEE.getTimeStamp(), OEE.getAssetID(), "report")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }

        }
        return msg;
    }
}
