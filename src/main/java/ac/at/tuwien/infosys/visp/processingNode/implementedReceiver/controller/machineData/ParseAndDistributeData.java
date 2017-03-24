package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.peerJ.MachineData;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ParseAndDistributeData extends GeneralController {

    private static final Logger LOG = LoggerFactory.getLogger(ParseAndDistributeData.class);

    public Message process(Message message) {

        MachineData md = null;

        String file = parse(message);
        try {
            md = parseMachineData(file);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = msgutil.createEmptyMessage();
        try {
            msg = msgutil.createMessage("distributedata", ow.writeValueAsBytes(md));
        } catch (JsonProcessingException e) {
            error.send(e.getMessage());
        }

        return msg;
    }


    private String parse(Message message) {
        try {
            File imageFile = File.createTempFile("img", ".png");
            FileUtils.writeByteArrayToFile(imageFile, message.getBody());
            File resultFile = File.createTempFile("result", ".txt");

            Process pr = Runtime.getRuntime().exec("tesseract " + imageFile.getAbsolutePath() + " " + resultFile.getAbsolutePath());

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                error.send(line);
                LOG.info(line);
            }
            pr.waitFor();

            imageFile.delete();

            return (resultFile.getAbsolutePath() + ".txt");

        } catch (Exception e) {
            error.send(e.getMessage());
        }
        return "";
    }


    private MachineData parseMachineData(String filename) throws IOException {
        Map<String, String> data = parseKeyValue(filename);
        MachineData md = new MachineData();
        md.setId(UUID.randomUUID().toString());
        md.setAssetID(getItem(data, "assetid"));
        md.setMachineType(getItem(data, "machinetype"));
        md.setLocation(getItem(data, "location"));
        try {
            md.setProducedUnits(Integer.valueOf(getItem(data, "producedunits")));
        } catch (Exception e) {
            md.setProducedUnits(5);
        }
        try {
            md.setDefectiveUnits(Integer.valueOf(getItem(data, "defectiveunits")));
        } catch (Exception e) {
            md.setDefectiveUnits(2);
        }
        try {
            md.setPlannedProductionTime(Integer.valueOf(getItem(data, "plannedproductiontime")));
        } catch (Exception e) {
            md.setPlannedProductionTime(2);
        }

        md.setActive(getItem(data, "active"));
        md.setTimestamp(getItem(data, "timestamp"));

        return md;
    }

    private String getItem(Map<String, String> map, String key) throws IOException {
        if (map.containsKey(key)) {
            return map.get(key).replace("»", "-");
        } else {
            error.send(key + " not available");
            return "not available";
        }
    }


    private Map<String, String> parseKeyValue(String filename) throws IOException {
        Map<String, String> entries = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(":")) {
                try {
                    String[] parts = line.split(":");
                    entries.put(parts[0].trim().toLowerCase(), parts[1].trim());
                } catch (Exception e) {
                }
            }
        }
        reader.close();
        Files.deleteIfExists(Paths.get(filename));
        return entries;
    }

}
