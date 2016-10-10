package ac.at.tuwien.infosys.visp.controller.peerj;

import ac.at.tuwien.infosys.visp.DurationHandler;
import ac.at.tuwien.infosys.visp.ErrorHandler;
import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransformData {

    @Autowired
    ErrorHandler error;

    @Autowired
    DurationHandler duration;

    private static final Logger LOG = LoggerFactory.getLogger(TransformData.class);


    public Message forwardMessagewithWait(Message message) {

        Message msg = new Message("transformedData", message.getPayload());

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //send a response on average every 5 messages
        if ((int)(Math.random() * 10) == 1) {
            duration.send(message.getProcessingDuration());
        }


        return message;
    }

}
