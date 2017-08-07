package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.common.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandler {

    @Value("${role}")
    private String role;

    @Value("${hostname}")
    private String containerid;

    @Value("${operatorname}")
    private String operatorname;

    @Value("${operatortype}")
    private String operatortype;

    private String errorexchange = "error";

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    public void send(String exception) {

        Message msg = new Message("error", operatorname, containerid, "operator: " + role + "\n\n" + exception);

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);
        connectionFactory.setUsername(rabbitmqUsername);
        connectionFactory.setPassword(rabbitmqPassword);


        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setRoutingKey(errorexchange);
        template.setQueue(errorexchange);
        template.convertAndSend(errorexchange, errorexchange, msg);
        connectionFactory.destroy();
    }
}




