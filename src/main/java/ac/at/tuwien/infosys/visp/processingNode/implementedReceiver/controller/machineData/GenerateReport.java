package ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.machineData;

import ac.at.tuwien.infosys.visp.common.peerJ.OEE;
import ac.at.tuwien.infosys.visp.common.peerJ.Warning;
import ac.at.tuwien.infosys.visp.processingNode.implementedReceiver.controller.GeneralController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GenerateReport extends GeneralController {

    @Autowired
    private StringRedisTemplate template;

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReport.class);


    public Message process(Message message) {
        ObjectMapper mapper = new ObjectMapper();
        OEE OEE = null;
        try {
            OEE = mapper.readValue(message.getBody(), OEE.class);
        } catch (IOException e) {
            error.send(e.getMessage());
        }

        ValueOperations<String, String> ops = this.template.opsForValue();

        //TODO generate fancy report
        // aggregate OEE value over time and generate a report where machines are grouped by location/type and it shows the difference compared to the last oee value (some trend)

        Message msg = msgutil.createEmptyMessage();

        if ((int) (Math.random() * 300) == 1) {

            List<OEE> values = new ArrayList<>();
            values.add(OEE);
            String pdfresult = "";
            try {
                pdfresult = Base64.getEncoder().encodeToString(createDocument(values).toByteArray());
            } catch (IOException e) {
                error.send(e.getLocalizedMessage());
            }


            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            try {
                msg = msgutil.createMessage("warning", ow.writeValueAsBytes(new Warning(pdfresult, OEE.getTimeStamp(), OEE.getAssetID(), "report")));
            } catch (JsonProcessingException e) {
                error.send(e.getMessage());
            }

        }
        return msg;
    }

    private ByteArrayOutputStream createDocument(List<OEE> values) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage( page );

        PDFont font = PDType1Font.HELVETICA_BOLD;
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.beginText();
        contentStream.setFont( font, 12 );
        contentStream.newLineAtOffset( 100, 700 );
        contentStream.showText( "OEE value: " + values.get(0).getOee() );
        contentStream.endText();

        contentStream.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out;
    }

}
