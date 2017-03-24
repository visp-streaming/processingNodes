package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class WaitController extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(WaitController.class);

    private Random rnd;
    
    public WaitController() {
    	this.rnd = new Random();
	}

    public Message process(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        Message msg = msgutil.createEmptyMessage();

        try {
        switch(new String(message.getBody())) {
            case "step1" : Thread.sleep(100); msg = msgutil.createMessage("wait", "step2");  break;
            case "step2" : Thread.sleep(250); msg = msgutil.createMessage("wait", "step3");  break;
            case "step3" : Thread.sleep(500); msg = msgutil.createMessage("wait", "step4");  break;
            case "step4" : Thread.sleep(1000); msg = msgutil.createMessage("wait", "step5");  break;
            case "step5" : Thread.sleep(2000); msg = msgutil.createMessage("log", "log");  break;
            default : Thread.sleep(100); msg = msgutil.createMessage("log", "log");
        }

        } catch (InterruptedException e) {
            error.send(e.getMessage());
        }

        LOG.info("Log message with: " + message.getMessageProperties().getMessageId() + " " + msgutil.getHeader(message) + new String(message.getBody()));
        LOG.info("Log emitting with: " + msg.getMessageProperties().getMessageId() + " " + msgutil.getHeader(msg) + new String(msg.getBody()));


        //send a response on average every 5 messages
        if ((int)(Math.random() * 5) == 1) {
            duration.send(msgutil.getDuration(message));
        }


        return msg;
    }

    public Message waitAndForwardByRole(String role, Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        Message msg = msgutil.createMessage(msgutil.getHeader(message), message.getBody());
        msg.getMessageProperties().setMessageId(message.getMessageProperties().getMessageId());

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

        LOG.info("Log message with: " + message.getMessageProperties().getMessageId() + " " + msgutil.getHeader(message) + new String(message.getBody()));
        LOG.info("Log emitting with: " + msg.getMessageProperties().getMessageId() + " " + msgutil.getHeader(msg) + new String(msg.getBody()));


        //send a response on average every 10 messages
        if ((int)(Math.random() * 10) == 1) {
            duration.send(msgutil.getDuration(message));
        }

        return msg;
    }

}
