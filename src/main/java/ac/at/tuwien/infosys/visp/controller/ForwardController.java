package ac.at.tuwien.infosys.visp.controller;

import entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ForwardController {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardController.class);


    @RequestMapping(value = "/forward", method = RequestMethod.POST)
    public String forwardMessage(@RequestBody Message message) {
        LOG.info("Received message with id: " + message.getId());

        RestTemplate restTemplate = new RestTemplate();

        //TODO make url for masternode flexible (set it while spawning a new VM)
        //restTemplate.put("http://localhost:10000/processedResult", message, Message.class);

        LOG.info("Forwarded message with id: " + message.getId());
        return "OK";
    }


}
