package ac.at.tuwien.infosys.visp;


import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandler {

    @Value("${role}")
    private String role;

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandler.class);

    private String errorexchange = "error";

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    public void send(String exception) {


        Message msg = new Message("error", "operator: " + role + "\n\n" + exception);

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(errorexchange);
        template.setQueue(errorexchange);
        template.convertAndSend(errorexchange, errorexchange, msg);
        connectionFactory.destroy();
    }
}