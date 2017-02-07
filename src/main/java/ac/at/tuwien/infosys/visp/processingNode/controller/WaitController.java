package ac.at.tuwien.infosys.visp.processingNode.controller;

import ac.at.tuwien.infosys.visp.processingNode.DurationHandler;
import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class WaitController {

    @Autowired
    ErrorHandler error;

    @Autowired
    DurationHandler duration;

    private static final Logger LOG = LoggerFactory.getLogger(WaitController.class);

    private Random rnd;
    
    public WaitController() {
    	this.rnd = new Random();
	}

    public Message forwardMessagewithWait(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        Message msg = new Message("wait");

        try {
        switch(message.getPayload()) {
            case "step1" : Thread.sleep(100); msg = new Message("wait", "step2");  break;
            case "step2" : Thread.sleep(250); msg = new Message("wait", "step3");  break;
            case "step3" : Thread.sleep(500); msg = new Message("wait", "step4");  break;
            case "step4" : Thread.sleep(1000); msg = new Message("wait", "step5");  break;
            case "step5" : Thread.sleep(2000); msg = new Message("log", "log");  break;
            default : Thread.sleep(100); msg = new Message("log", "log");
        }

        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());
        LOG.info("Log emitting with: " + msg.getId() + " " + msg.getHeader() + msg.getPayload());


        //send a response on average every 5 messages
        if ((int)(Math.random() * 5) == 1) {
            duration.send(message.getProcessingDuration());
        }


        return msg;
    }

    public Message waitAndForwardByRole(String role, Message message) {
        LOG.trace("Received message with id: " + message.getId());

        Message msg = new Message(message.getHeader(), "message");
        msg.setId(message.getId());

        try {
        	long wait = 0;
	        switch(role.toLowerCase()) {
	            case "step1" : 
	            	wait = (long) Math.floor(rnd.nextDouble() * 12.0 + 13.0);
	            	Thread.sleep(wait); 
	            	break;
	            case "step2" : 
	            	wait = (long) Math.floor(rnd.nextDouble() * 9.0 + 30.0);
	            	Thread.sleep(wait); 
	            	break;
	            case "consumer" : 
	            	wait = (long) Math.floor(rnd.nextDouble() * 5.0 + 8.0);
	            	Thread.sleep(wait); 
	            	break;
	            default : 
	            	Thread.sleep(100);
	        }
        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());
        LOG.info("Log emitting with: " + msg.getId() + " " + msg.getHeader() + msg.getPayload());


        //send a response on average every 10 messages
        if ((int)(Math.random() * 10) == 1) {
            duration.send(message.getProcessingDuration());
        }

        return msg;
    }

}
