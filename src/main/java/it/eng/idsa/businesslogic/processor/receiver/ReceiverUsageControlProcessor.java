package it.eng.idsa.businesslogic.processor.receiver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.fraunhofer.dataspaces.iese.camel.interceptor.model.Meta;
import de.fraunhofer.dataspaces.iese.camel.interceptor.model.TargetArtifact;
import de.fraunhofer.dataspaces.iese.camel.interceptor.model.UsageControlObject;
import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;


/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */
@ComponentScan("de.fraunhofer.dataspaces.iese")
@Component
public class ReceiverUsageControlProcessor implements Processor {

    private Gson gson;
    private static final Logger logger = LoggerFactory.getLogger(ReceiverUsageControlProcessor.class);

    private Message requestMessage;
    private Message responseMessage;

    @Value("${application.isEnabledUsageControl:false}")
    private boolean isEnabledUsageControl;

    @Autowired
    private MultipartMessageService multipartMessageService;

    @Autowired
    private RejectionMessageService rejectionMessageService;

    public ReceiverUsageControlProcessor() {
        gson = createGson();
    }

    @Override
    public void process(Exchange exchange) {
        boolean isUsageControlObject = true;
        if (!isEnabledUsageControl) {
            logger.info("Usage control not configured - continued with flow");
            return;
        }
        try {
            Map<String, Object> headerParts = exchange.getMessage().getHeaders();
            MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
            String originalHeader = headerParts.get("Original-Message-Header").toString();
            requestMessage = multipartMessageService.getMessage(originalHeader);
            responseMessage = multipartMessage.getHeaderContent();
            ArtifactRequestMessage artifactRequestMessage = null;
            if(null == requestMessage || null == responseMessage)
                throw new Exception("Request or Response messages are empty.");
            
//            if (!(requestMessage instanceof  ArtifactRequestMessage
//                    && (responseMessage instanceof ArtifactResponseMessage)
//                    && !(responseMessage instanceof RejectionMessage))) {
//                throw new Exception("Response Header Type is not compatible with Usage Control Required.");
//            }
//            if (requestMessage instanceof ArtifactRequestMessage
//                    && !(responseMessage instanceof ArtifactResponseMessage)) {
//                throw new Exception("Response Header Type is not compatible with Usage Control Required.");
//            } else {
//            	isUsageControlObject = false;
//            }
            try {
                artifactRequestMessage = (ArtifactRequestMessage) requestMessage;
            } catch (Exception e) {
            	logger.info("RequestMessage not of instance ArtifactRequestMessage - will not apply Usage Control");
                isUsageControlObject = false;
            }
            if (isUsageControlObject) {
                String payloadToEnforce = createUsageControlObject(artifactRequestMessage.getRequestedArtifact(),
                        multipartMessage.getPayloadContent());
                logger.info("from: " + exchange.getFromEndpoint());
                logger.debug("Message Body: " + payloadToEnforce);
                logger.info("Message Body Out: " + exchange.getMessage().getBody(String.class));

                MultipartMessage reponseMultipartMessage = new MultipartMessageBuilder()
                		.withHttpHeader(multipartMessage.getHttpHeaders()).withHeaderHeader(multipartMessage.getHeaderHeader())
        				.withHeaderContent(responseMessage).withPayloadHeader(multipartMessage.getPayloadHeader())
        				.withPayloadContent(payloadToEnforce).withToken(multipartMessage.getToken())
        				.build();
//                exchange.getIn().setBody(reponseMultipartMessage);
                exchange.getMessage().setBody(reponseMultipartMessage);
            }
            	headerParts.remove("Original-Message-Header");
//            	exchange.getMessage().setBody(exchange.getIn().getBody());
//            	exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
            
        } catch (Exception e) {
            logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
            rejectionMessageService.sendRejectionMessage(
                    RejectionMessageType.REJECTION_USAGE_CONTROL,
                    requestMessage);
        }
    }

    public static Gson createGson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter writer, ZonedDateTime zdt) throws IOException {
                writer.value(zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        }).enableComplexMapKeySerialization().create();

        return gson;
    }


    private String createUsageControlObject(URI targetId, String payload) throws URISyntaxException {
        UsageControlObject usageControlObject = new UsageControlObject();
        JsonElement jsonElement = gson.fromJson(createJsonPayload(payload), JsonElement.class);
        usageControlObject.setPayload(jsonElement);
        Meta meta = new Meta();
        meta.setAssignee(requestMessage.getIssuerConnector());
        meta.setAssigner(responseMessage.getIssuerConnector());
        TargetArtifact targetArtifact = new TargetArtifact();
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("CET"));
        targetArtifact.setCreationDate(zonedDateTime);
        targetArtifact.setId(targetId);
        meta.setTargetArtifact(targetArtifact);
        usageControlObject.setMeta(meta);
        String usageControlObjectPayload = gson.toJson(usageControlObject, UsageControlObject.class);
        return usageControlObjectPayload;
    }

    private String createJsonPayload(String payload) {
        boolean isJson = true;
        try {
            JsonParser.parseString(payload);
        } catch (JsonSyntaxException e) {
            isJson = false;
        }
        if (!isJson) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{");
            stringBuilder.append("\"payload\":" + "\"" + payload + "\"");
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
        return payload;
    }

}
