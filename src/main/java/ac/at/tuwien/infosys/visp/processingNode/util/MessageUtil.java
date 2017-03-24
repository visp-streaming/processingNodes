package ac.at.tuwien.infosys.visp.processingNode.util;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

@Service
public class MessageUtil {

    public Message createMessage(String type, String payload) {
        MessageProperties props = new MessageProperties();
        props.setHeader("type", type);
        Message message = new Message(payload.getBytes(), props);
        return message;
    }

    public Message createMessage(String type, byte[] payload) {
        MessageProperties props = new MessageProperties();
        props.setHeader("type", type);
        Message message = new Message(payload, props);
        return message;
    }

    public Message createEmptyMessage() {
        MessageProperties props = new MessageProperties();
        props.setHeader("type", "empty");
        Message message = new Message(null, props);
        return message;
    }

    public String getHeader(Message message) {
        return (String) message.getMessageProperties().getHeaders().get("type");
    }

    public String getDuration(Message message) {
        return (String) message.getMessageProperties().getHeaders().get("duration");
    }


}
