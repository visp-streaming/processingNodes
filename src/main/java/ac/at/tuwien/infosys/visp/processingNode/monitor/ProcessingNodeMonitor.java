package ac.at.tuwien.infosys.visp.processingNode.monitor;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The ProcessingNodeMonitor collects statistics on the execution of the
 * operator instance (i.e., the processing node).
 *
 */
@Service
public class ProcessingNodeMonitor {

	private Map<String, Long> emittedMessages;
	private Map<String, Long> processedMessages;
	
	private Lock emittedMessagesLock;
	private Lock processedMessagesLock;

	public ProcessingNodeMonitor() {
		emittedMessages = new HashMap<>();
		processedMessages = new HashMap<>();

		emittedMessagesLock = new ReentrantLock();
		processedMessagesLock = new ReentrantLock();
	}

	public void notifyOutgoingMessage(List<String> destinationOperators) {

		emittedMessagesLock.lock();
		try{

			for (String operatorName : destinationOperators){
				updateOutgoingMessage(operatorName);
			}

		} finally{
			emittedMessagesLock.unlock();
		}

	}

	public void notifyOutgoingMessage(String destinationOperatorName) {

		emittedMessagesLock.lock();
		try{

			updateOutgoingMessage(destinationOperatorName);

		} finally{
			emittedMessagesLock.unlock();
		}

	}

	private void updateOutgoingMessage(String destinationOperatorName) {

		Long count = emittedMessages.get(destinationOperatorName);

		if (count == null)
			count = new Long(1);
		else
			count = new Long(count.longValue() + 1);

		emittedMessages.put(destinationOperatorName, count);			

	}

	
	public void notifyProcessedMessage(String operatorName) {

		processedMessagesLock.lock();
		
		try{

			Long count = processedMessages.get(operatorName);

			if (count == null)
				count = new Long(1);
			else
				count = new Long(count.longValue() + 1);

			processedMessages.put(operatorName, count);

		}finally{
			processedMessagesLock.unlock();
		}

	}
	
	public Map<String, Long> getAndResetEmittedMessages(){
		
		Map<String, Long> lastEmittedMessages  = null;
		emittedMessagesLock.lock();
		
		try{
			lastEmittedMessages = emittedMessages;
			emittedMessages = new HashMap<>();
		}finally{
			emittedMessagesLock.unlock();
		}

		return lastEmittedMessages;

	}
	public Map<String, Long> getAndResetProcessedMessages(){
		
		Map<String, Long> lastProcessedMessages = null;
		processedMessagesLock.lock();
		
		try{
			lastProcessedMessages = processedMessages;
			processedMessages = new HashMap<>();
		}finally{
			processedMessagesLock.unlock();
		}
		
		return lastProcessedMessages;
	}
	
}
