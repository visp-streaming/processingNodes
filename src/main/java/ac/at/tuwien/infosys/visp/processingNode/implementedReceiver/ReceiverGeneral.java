package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver;


import ac.at.tuwien.infosys.visp.processingNode.Receiver;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.WaitController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "visp.receiver", havingValue = "general")
public class ReceiverGeneral extends Receiver {

    @Autowired
    private WaitController waitController;

    private static final Logger LOG = LoggerFactory.getLogger(ReceiverGeneral.class);

    public void assign(Message message) throws InterruptedException {

        switch (role.toLowerCase()) {
            case "source":
                LOG.info("role setted as source! Message discarded");
                break;
            case "step1":
                LOG.info("step1 has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
                break;
            case "step2":
                LOG.info("step2 has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
                break;

            //TODO reenable if required
            //case "monitor":
            //    appMonitor.handleMessage(APPNAME, message);
            //    break;
            default:
                LOG.info(role + " has received a message");
                sender.send(waitController.waitAndForwardByRole(role, message));
        }
    }
}






