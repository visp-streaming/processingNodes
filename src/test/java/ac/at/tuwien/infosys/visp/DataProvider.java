package ac.at.tuwien.infosys.visp;

import entities.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = VispProcessingNodeApplication.class)
public class DataProvider {

    @Value("${outgoingexchange}")
    private String outgoingExchange;

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;


    @Test
    @Repeat(value = 10)
    public void send() {
        Message msg = new Message("test123");

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(outgoingExchange);
        template.setQueue(outgoingExchange);
        template.convertAndSend(outgoingExchange, outgoingExchange, msg);
        connectionFactory.destroy();
    }

    @Test
    public void senderror() {
        Message msg = new Message("error", "errormessage");

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey("error");
        template.setQueue("error");
        template.convertAndSend("error", "error", msg);
        connectionFactory.destroy();
    }
}
