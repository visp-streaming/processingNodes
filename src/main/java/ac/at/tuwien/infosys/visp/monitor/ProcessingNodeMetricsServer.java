package ac.at.tuwien.infosys.visp.monitor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import entities.ProcessingNodeMetricsMessage;

/**
 * The ProcessingNodeMetricsServer exposes the collected statistics 
 * on the execution of the operator instance (i.e., the processing node).
 *
 */

@RestController
public class ProcessingNodeMetricsServer {

	@Autowired
	private ProcessingNodeMonitor procNodeMonitor;
	
    @Value("${role}")
    private String role;
	
    @RequestMapping("/metrics")
    public ProcessingNodeMetricsMessage sendAndResetStatistics() {

    	Map<String, Long> emittedMessages = procNodeMonitor.getAndResetEmittedMessages();
    	Map<String, Long> processedMessages = procNodeMonitor.getAndResetProcessedMessages();
    	
    	ProcessingNodeMetricsMessage message = new ProcessingNodeMetricsMessage();
    	message.setProcessingNode(role);
    	message.setProcessedMessages(processedMessages);
    	message.setEmittedMessages(emittedMessages);
    	
    	return message; 
    }	
    
}
