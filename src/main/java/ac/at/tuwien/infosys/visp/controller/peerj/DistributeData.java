package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.ErrorHandler;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DistributeData {

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(DistributeData.class);

    public Message process(Message message) {

        Message msg = new Message("distributedata", message.getPayload());

        return msg;
    }

}
