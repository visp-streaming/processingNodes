package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.common.Message;
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

        Message msg = new Message("forward");

        LOG.info("Do nothing with: " + message.getId());

        switch(message.getPayload()) {
            case "step1" : msg = new Message("forward", "step2");  break;
            case "step2" : msg = new Message("forward", "step3");  break;
            case "step3" : msg = new Message("forward", "step4");  break;
            case "step4" : msg = new Message("forward", "step5");  break;
            case "step5" :  msg = new Message("log", "log");  break;
            default : msg = new Message("log", "log");
        }

        LOG.trace("Forwarded message with id: " + message.getId());

        return msg;
    }

}
