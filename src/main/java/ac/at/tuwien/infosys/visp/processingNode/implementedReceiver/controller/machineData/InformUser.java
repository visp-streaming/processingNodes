package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

@Service
public class InformUser extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(InformUser.class);

    public Message process(Message message) {

        //TODO consume messages


        return msgutil.createEmptyMessage();
    }

}
