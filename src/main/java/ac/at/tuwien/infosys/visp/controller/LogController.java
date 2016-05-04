package ac.at.tuwien.infosys.visp.controller;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogController {

    @Value("${wait.log}")
    private Integer wait;

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(LogController.class);


    public Message logMessage(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());

        Message msg = new Message("empty", null);

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }


        return msg;
    }

}
