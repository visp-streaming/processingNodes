package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.simulateUsage;

import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CPUController {

    @Autowired
    private ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(CPUController.class);

    public Message generateCPULoad(Message message) {
        LOG.trace("Received message with id: " + message.getId());

        LOG.info("Log message with: " + message.getId() + " " + message.getHeader() + message.getPayload());

        Message msg = new Message("empty", null);

        return msg;
    }
}
