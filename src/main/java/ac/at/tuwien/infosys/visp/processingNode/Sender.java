package ac.at.tuwien.infosys.visp.processingNode;


import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.monitor.ProcessingNodeMonitor;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    
    @Value("${operator.subscribed.operators}")
    private String subscribedOperators;

    @Autowired
    private ProcessingNodeMonitor monitor;
    
    private List<String> destinations;

    @PostConstruct
    private void computeDestinationOperatorName(){
    	
    	destinations = new ArrayList<>();

    	Iterable<String> dwnStr = Splitter.on(',').split(subscribedOperators);
    	if (dwnStr == null) {
            return;
        }

    	Iterator<String> it = dwnStr.iterator();
    	while(it.hasNext()) {
    		destinations.add(it.next());
    	}
    	
    	LOG.info("Set of downstream operators updated. New set: " + destinations);
    }

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

        /* With current implementation, the outgoing message
         * is forwarded to all downstream queues. */
        monitor.notifyOutgoingMessage(destinations);
    }
    
}