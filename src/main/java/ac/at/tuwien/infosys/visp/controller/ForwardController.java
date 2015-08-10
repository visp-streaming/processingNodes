package ac.at.tuwien.infosys.visp.controller;

import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ForwardController {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);


    @RequestMapping(value = "/forward", method = RequestMethod.POST)
    public Message forwardMessage(@RequestBody Message message) {
        LOG.info("Received message with id: " + message.getId());

        LOG.info("Do nothing with: " + message.getId());

        LOG.info("Forwarded message with id: " + message.getId());
        return message;
    }

}
