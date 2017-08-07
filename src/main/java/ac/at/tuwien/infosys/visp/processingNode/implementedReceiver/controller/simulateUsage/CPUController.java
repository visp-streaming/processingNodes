package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.simulateUsage;

import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.processingNode.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CPUController {

    @Autowired
    private ErrorHandler error;

    @Autowired
    private MessageUtil msgutil;

    private static final Logger LOG = LoggerFactory.getLogger(CPUController.class);

    public Message generateCPULoad(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        LOG.info("Log message with: " +  message.getMessageProperties().getMessageId() + " " + msgutil.getHeader(message) + new String(message.getBody()));

        //TODO implement me

        Message msg = msgutil.createEmptyMessage();

        return msg;
    }
}
