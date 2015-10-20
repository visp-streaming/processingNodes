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

    //TODO fetch outgoing exchange from environment variables

    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    @Value("${rabbitMQ.outgoingQueue}")
    private String outgoingQueue;

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    public void send(Message message) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(outgoingQueue);
        template.setQueue(outgoingQueue);
        template.convertAndSend(outgoingQueue, message);
        connectionFactory.destroy();
    }
}