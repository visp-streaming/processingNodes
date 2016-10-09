package ac.at.tuwien.infosys.visp.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

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
		emittedMessages = new HashMap<String, Long>();
		processedMessages = new HashMap<String, Long>();

		emittedMessagesLock = new ReentrantLock();
		processedMessagesLock = new ReentrantLock();
	}

	public void notifyOutgoingMessage(String destinationOperatorName) {

		emittedMessagesLock.lock();
		try{
		
			Long count = emittedMessages.get(destinationOperatorName);

			if (count == null)
				count = new Long(1);
			else
				count = new Long(count.longValue() + 1);

			emittedMessages.put(destinationOperatorName, count);			

		} finally{
			emittedMessagesLock.unlock();
		}

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
			emittedMessages = new HashMap<String, Long>();
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
			processedMessages = new HashMap<String, Long>();
		}finally{
			processedMessagesLock.unlock();
		}
		
		return lastProcessedMessages;
	}
	
}
