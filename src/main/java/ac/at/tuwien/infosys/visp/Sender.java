package ac.at.tuwien.infosys.visp;


import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Sender {

    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    @Value("${outgoingexchange}")
    private String outgoingExchange;

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    public void send(Message message) {
        if (message.getHeader().equals("empty")) {
            return;
        }

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);
        connectionFactory.setUsername(rabbitmqUsername);
        connectionFactory.setPassword(rabbitmqPassword);


        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(outgoingExchange);
        template.setQueue(outgoingExchange);
        template.convertAndSend(outgoingExchange, outgoingExchange, message);
        connectionFactory.destroy();
    }
}