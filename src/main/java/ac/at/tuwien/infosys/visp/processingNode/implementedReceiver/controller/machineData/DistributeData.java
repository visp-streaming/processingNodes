package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.Message;
import ac.at.tuwien.infosys.visp.common.peerJ.MachineData;
import ac.at.tuwien.infosys.visp.processingNode.ErrorHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DistributeData {

    @Autowired
    ErrorHandler error;

    private static final Logger LOG = LoggerFactory.getLogger(DistributeData.class);

    public Message process(Message message) {

        MachineData md = null;

        String file = parse(message);
        try {

            md = parseMachineData(file);
        } catch (IOException e) {
            error.send(e.getLocalizedMessage());
        }

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Message msg = new Message("empty", null);
        try {
            msg = new Message("distributedata", ow.writeValueAsString(md));
        } catch (JsonProcessingException e) {
            error.send(e.getLocalizedMessage());
        }

        return msg;
    }


    private String parse(Message message) {
        try {
            File imageFile = File.createTempFile("img", ".png");
            FileUtils.writeByteArrayToFile(imageFile, Base64.getDecoder().decode(message.getPayload()));
            File resultFile = File.createTempFile("result", ".txt");

            Process pr = Runtime.getRuntime().exec("tesseract " + imageFile.getAbsolutePath() + " " + resultFile.getAbsolutePath());

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                error.send(line);
            }
            pr.waitFor();

            return (resultFile.getAbsolutePath() + ".txt");

        } catch (Exception e) {
            error.send(e.getLocalizedMessage());
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
        md.setProducedUnits(Integer.valueOf(getItem(data, "producedunits")));
        md.setDefectiveUnits(Integer.valueOf(getItem(data, "defectiveunits")));
        md.setPlannedProductionTime(Integer.valueOf(getItem(data, "plannedproductiontime")));
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
        Map<String, String> entries = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(":")) {
                String[] parts = line.split(":");
                entries.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        }
        reader.close();
        return entries;
    }

}
