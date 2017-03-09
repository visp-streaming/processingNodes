package ac.at.tuwien.infosys.visp.processingNode.util;

public class QueueDefinition {
    public String infrastructureHost;
    public String senderOperator;
    public String receiverOperator;

    public QueueDefinition(String infrastructureHost, String senderOperator, String receiverOperator) {
        this.infrastructureHost = infrastructureHost;
        this.senderOperator = senderOperator;
        this.receiverOperator = receiverOperator;
    }

    public QueueDefinition(String identifier) {
        String[] hostAndRemainder = identifier.split("/");
        infrastructureHost = hostAndRemainder[0];
        String[] senderAndReceiver = hostAndRemainder[1].split(">");
        senderOperator = senderAndReceiver[0];
        receiverOperator = senderAndReceiver[1];
        this.infrastructureHost = infrastructureHost;
        this.receiverOperator = receiverOperator;
        this.senderOperator = senderOperator;
    }

    @Override
    public String toString() {
        return infrastructureHost + "/" + senderOperator + ">" + receiverOperator;
    }

}