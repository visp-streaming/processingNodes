package ac.at.tuwien.infosys.visp.controller;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ForwardController {


    @Value("${wait.forward}")
    private Integer wait;

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);


    @RequestMapping(value = "/forward", method = RequestMethod.POST)
    public Message forwardMessage(@RequestBody Message message) {
        LOG.trace("Received message with id: " + message.getId());

        LOG.info("Do nothing with: " + message.getId());

        LOG.trace("Forwarded message with id: " + message.getId());

        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }


        return message;
    }

}
