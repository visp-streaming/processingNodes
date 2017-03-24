package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ForwardController extends GeneralController {


    @Value("${wait.forward}")
    private Integer wait;

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);

    public Message process(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        Message msg = msgutil.createEmptyMessage();

        switch(new String(message.getBody())) {
            case "step1" : msg = msgutil.createMessage("forward", "step2");  break;
            case "step2" : msg = msgutil.createMessage("forward", "step3");  break;
            case "step3" : msg = msgutil.createMessage("forward", "step4");  break;
            case "step4" : msg = msgutil.createMessage("forward", "step5");  break;
            case "step5" :  msg = msgutil.createMessage("log", "log");  break;
            default : msg = msgutil.createMessage("log", "log");
        }

        LOG.trace("Forwarded message with id: " + message.getMessageProperties().getMessageId());

        return msg;
    }

}
