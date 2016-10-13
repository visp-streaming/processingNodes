package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Message;
import entities.peerJ.OOE;
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
        OOE ooe = null;
        try {
            ooe = mapper.readValue(message.getPayload(), OOE.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        //TODO aggregate OOE value over time and generate a report where machines are grouped by location/type and it shows the difference compared to the last ooe value (some trend)

        Message msg = new Message("empty", null);

/*
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            msg = new Message("ooeavailability", ow.writeValueAsString(ooeavailability));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }
        */

        return msg;
    }
}
