package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogController extends GeneralController{

    @Value("${wait.log}")
    private Integer wait;

    private static final Logger LOG = LoggerFactory.getLogger(LogController.class);


    public Message process(Message message) {
        LOG.trace("Received message with id: " + message.getMessageProperties().getMessageId());

        LOG.info("Log message with: " +  message.getMessageProperties().getMessageId() + " " + msgutil.getHeader(message) + new String(message.getBody()));

        return msgutil.createEmptyMessage();
    }
}
