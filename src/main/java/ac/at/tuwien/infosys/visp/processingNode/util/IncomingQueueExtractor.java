package ac.at.tuwien.infosys.visp.processingNode.util;

import java.util.ArrayList;
import java.util.List;

public class IncomingQueueExtractor {
    private String incomingQueuesString;
    private String infrastructureHost;
    private String senderOperator;
    private String receiverOperator;


    public IncomingQueueExtractor(String incomingQueuesString, String infrastructureHost, String senderOperator, String receiverOperator) {
        this.incomingQueuesString = incomingQueuesString;
        this.infrastructureHost = infrastructureHost;
        this.senderOperator = senderOperator;
        this.receiverOperator = receiverOperator;
    }

    public List<QueueDefinition> getQueueDefinitions() {
        try {
            String[] incomingQueues = incomingQueuesString.split("_");

            List<QueueDefinition> returnList = new ArrayList<>();
            for (String queue : incomingQueues) {
                returnList.add(new QueueDefinition(queue));
            }

            return returnList;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse list of incoming queues", e);
        }
    }
}