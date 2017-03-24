package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller;

import ac.at.tuwien.infosys.visp.processingNode.DurationHandler;
import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import ac.at.tuwien.infosys.visp.processingNode.util.MessageUtil;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class GeneralController {

    @Autowired
    protected MessageUtil msgutil;

    @Autowired
    protected ErrorHandler error;

    @Autowired
    protected DurationHandler duration;

    abstract public Message process(Message message);

}
