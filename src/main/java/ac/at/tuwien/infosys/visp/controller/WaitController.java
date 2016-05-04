package ac.at.tuwien.infosys.visp.controller;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WaitController {

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(WaitController.class);


    public Message forwardMessagewithWait(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        Message msg = new Message("wait");

        try {
        switch(message.getPayload()) {
            case "step1" : Thread.sleep(100); msg = new Message("wait", "step2");  break;
            case "step2" : Thread.sleep(250); msg = new Message("wait", "step3");  break;
            case "step3" : Thread.sleep(500); msg = new Message("wait", "step4");  break;
            case "step4" : Thread.sleep(1000); msg = new Message("wait", "step5");  break;
            case "step5" : Thread.sleep(2000); msg = new Message("log");  break;
            default : Thread.sleep(100); msg = new Message("log");
        }

        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());

        return message;
    }

}
