package ac.at.tuwien.infosys.visp.controller;

import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogController {

    private static final Logger LOG = LoggerFactory.getLogger(LogController.class);


    public Message forwardMessage(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());

        Message msg = new Message("empty", null);
        return msg;
    }

}
