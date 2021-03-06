package ac.at.tuwien.infosys.visp.processingNode.monitor;

import ac.at.tuwien.infosys.visp.common.ApplicationQoSMetricsMessage;
import ac.at.tuwien.infosys.visp.common.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The ApplicationMonitorOperator collects and sends QoS metrics related
 * to the application (topology) executed on the VISP environment.
 */
@Service
public class ApplicationMonitorOperator {


    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    @Value("${spring.rabbitmq.outgoing.host}")
    private String outgoingHost;

    @Value("${application.monitor.expirationperiod}")
    private Long expirationPeriod;

    @Value("${application.monitor.send.every}")
    private Integer sendEvery;

    private String rabbitQueue = "applicationmetrics";

    Map<String, Triple> messageIdToTriple = new HashMap<String, ApplicationMonitorOperator.Triple>();
    
    List<Double> responseTimes = new ArrayList<>();
    private Lock responseTimesLock = new ReentrantLock();
    
    private int waitToSend = 10;
    private long lastCleanup = 0;
    private long cleanerGracePeriod = 500L;
    
    private Thread cleaner = null;
    
    @PostConstruct
    public void initialize(){
    	
    	/* Run in background the cleanup thread */
    	cleaner = new Thread(new BufferCleaner());
    	cleaner.start();
    	
    }
    
    public void handleMessage(String applicationName, Message message){
    
    	/* Check if this message notify the processing conclusion */
    	Triple triple = messageIdToTriple.get(message.getId());
    	
    	if (triple == null){
    		/* First observation of message id, register it as initial timestamp */
    		long now = System.currentTimeMillis();
    		triple = new Triple(now, message.getId(), now);

    		messageIdToTriple.put(message.getId(), triple);
    	} else {
    		/* Second observation of message id, compute processing delay */
    		long initialTimestamp = triple.getMessageInitialTimestamp();
    		long lastTriggeredEventTimestamp = System.currentTimeMillis();
        	double averageResponseTime = lastTriggeredEventTimestamp - initialTimestamp;

        	if (Double.isNaN(averageResponseTime)){
        		return;
        	}
        	
        	/* Send using a thread */
        	send(applicationName, averageResponseTime);
        	
        	/* Clear message-related data structures */
        	messageIdToTriple.remove(message.getId());
        	
    	}
    	
    }
    
    
    
    /**
     * This method sends the QoS metrics observed for the application. 
     * It should be executed on each application, receiving (or computing)
     * initialTimestamp from the data source and lastTriggeredEventTimestamp
     * from the final information consumer. 
     * 
     */
    private void send(String applicationName, double responseTime) {
    	
    	/* Update response time list, in a thread safe manner */
    	responseTimesLock.lock();
    	try {
    		responseTimes.add(new Double(responseTime));
    	} finally {
    		responseTimesLock.unlock();
    	}
    	
    	/* Aggregate and send statistics */
    	Thread sender = new Thread(new Sender(applicationName));
    	sender.start();
    }


    /**
     * Buffer cleanup thread
     * 
     * Expires message older then EXPIRATION_PERIOD
     */
    private class BufferCleaner implements Runnable{

    	BufferCleaner(){ }
    	
		@Override
		public void run() {
			
			while (true){
				
				try {
					/* Wake up every second */
					Thread.sleep(1000);
					
					/* Check if cleaner is in the grace period */
					if (lastCleanup + cleanerGracePeriod > System.currentTimeMillis())
						return;
					
					/* Clean-up buffer */
					long minTimestamp = System.currentTimeMillis() - expirationPeriod; 
					List<String> keyToRemove = new ArrayList<String>();
					List<Triple> triples = new ArrayList<Triple>(messageIdToTriple.values());

					/* Collect expired triples */
					for (Triple t : triples){
						if (t.isOlderThan(minTimestamp))
							keyToRemove.add(t.getId());
					}
					
					/* Remove them from the (shared) hashmap */
					for (String key : keyToRemove){
						messageIdToTriple.remove(key);
					}
					
					lastCleanup = System.currentTimeMillis();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
    }

    
    /**
     * Message Sender thread which avoids to block on RabbitMQ connection 
     */
    private class Sender implements Runnable{

    	private String applicationName;

    	Sender(String applicationName){
    		
    		this.applicationName = applicationName;
    		
    	}
    	
		@Override
		public void run() {
			
	    	if (waitToSend > 0){
	    		waitToSend--;
	    		return;
	    	}
	    	
	    	/* Check if some data needs to be sent */
	    	if (responseTimes.isEmpty())
	    		return;
	    	
	    	/* Compute average response time */
    		double averageResponseTime = 0;
        	responseTimesLock.lock();
        	try{
	    		for (Double rt : responseTimes)
	    			averageResponseTime += rt.doubleValue();
	    		averageResponseTime = averageResponseTime / (double) responseTimes.size();
	
	    		/* Clean list */
        		responseTimes.clear();
        	}finally{
        		responseTimesLock.unlock();
        	}
    		
	    	/* Create message */
	    	ApplicationQoSMetricsMessage message = new ApplicationQoSMetricsMessage(applicationName, averageResponseTime);

			/* Connect to RabbitMQ */
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(outgoingHost);
	        connectionFactory.setUsername(rabbitmqUsername);
	        connectionFactory.setPassword(rabbitmqPassword);

	    	/* Send message to RabbitMQ */
	        RabbitTemplate template = new RabbitTemplate(connectionFactory);
	        template.setRoutingKey(rabbitQueue);
	        template.setQueue(rabbitQueue);
	        template.convertAndSend(rabbitQueue, rabbitQueue, message);
	        
	        /* Close RabbitMQ connection */
	        connectionFactory.destroy();
			
	        /* Update grace period */
	        waitToSend = sendEvery;
		}
    	
    }

    
    /**
     * Short representation of application QoS-related metrics in the buffer
     */
    private class Triple{
    	
    	private long insertionTimestamp;
    	private String id; 
    	private long messageInitialTimestamp;
		
    	public Triple(long insertionTimestamp, String id,
				long messageInitialTimestamp) {
			super();
			this.insertionTimestamp = insertionTimestamp;
			this.id = id;
			this.messageInitialTimestamp = messageInitialTimestamp;
		}
		public String getId() {
			return id;
		}
		public long getMessageInitialTimestamp() {
			return messageInitialTimestamp;
		}
		public boolean isOlderThan(long timestamp){
			return (this.insertionTimestamp - timestamp < 0);
		}

		@Override
		public int hashCode() {
			return this.id.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Triple))
				return false;
			Triple other = (Triple) obj;
			return this.id.equals(other.id);
		}
    }
    
    
}




