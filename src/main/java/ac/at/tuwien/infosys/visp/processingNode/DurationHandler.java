package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.common.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DurationHandler {

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    @Value("${role}")
    private String role;

    private String processingdurationexchange = "processingduration";

    public void send(Date oldTime) {

        if (oldTime == null) {
            return;
        }

        Long duration = new Date().getTime() - oldTime.getTime();

        Message msg = new Message(role, duration.toString());

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);
        connectionFactory.setUsername(rabbitmqUsername);
        connectionFactory.setPassword(rabbitmqPassword);

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(processingdurationexchange);
        template.setQueue(processingdurationexchange);
        template.convertAndSend(processingdurationexchange, processingdurationexchange, msg);
        connectionFactory.destroy();
    }
}




