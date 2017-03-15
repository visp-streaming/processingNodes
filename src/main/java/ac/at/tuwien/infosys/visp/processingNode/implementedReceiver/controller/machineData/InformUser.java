package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.processingNode.DurationHandler;
import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InformUser {

    @Autowired
    ErrorHandler error;

    @Autowired
    DurationHandler duration;

    private static final Logger LOG = LoggerFactory.getLogger(InformUser.class);


    public Message process(Message message) {


        //TODO consume messages


        return new Message("empty", null);
    }

}
