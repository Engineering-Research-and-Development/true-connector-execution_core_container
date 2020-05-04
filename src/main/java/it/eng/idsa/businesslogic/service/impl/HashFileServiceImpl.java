package it.eng.idsa.businesslogic.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import it.eng.idsa.businesslogic.service.HashService;
import it.eng.idsa.clearinghouse.model.NotificationContent;
import it.eng.idsa.clearinghouse.model.json.JsonHandler;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

@Service
public class HashFileServiceImpl implements HashService {
    private static final Logger logger = LogManager.getLogger(HashFileServiceImpl.class);
    private static final String HASHING_ALGORITHM = "SHA-256"; //SHA3_256 most secure (no collisions) but less popular

    private String clearingHouseHashDir;

    public HashFileServiceImpl(@Value("${application.clearingHouseHashDir}") String clearingHouseHashDir) {
        this.clearingHouseHashDir = clearingHouseHashDir;
    }

    @Override
    public String hash(String payload) {
        if (StringUtils.isBlank(payload)) {
            logger.error("No Hashing was possible! Payload is empty");
            return null;
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASHING_ALGORITHM);
            byte[] encodedHash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hashing of Payload with NO Algorithm available to perform hashing: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void recordHash(String hash, String payload, NotificationContent notificationContent) {
        try {
            Path path = getPath(hash);
            if (StringUtils.isBlank(getClearingHouseHashDir())) {
                logger.error("No File PATH to record hash was found! application.clearingHouseHashFile" +
                        " or application.clearingHouseHashDir props are empty");
                return;
            }
            if (null == hash || null == notificationContent) {
                logger.error("No Hash Recording was possible! Hash or NotificationContent inputs are empty");
                return;
            }
            //Record the original payload not hashed!!!
            notificationContent.getBody().setPayload(payload);
            String content = writeJSON(hash, notificationContent);
            if (Files.notExists(path))
                Files.createFile(path);
            Files.write(
                    path,
                    content.getBytes(),
                    StandardOpenOption.CREATE);
            logger.info("Recorded Hash in FileSystem: " + content);
        } catch (Exception e) {
            logger.error("Recording of HASH in FileSystem encountered and error: " + e.getMessage());
        }
    }

    @Override
    public String getContent(String hash) throws Exception {
        Path path = getPath(hash);
        return getRecordFromFile(path);
    }


    private String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String writeJSON(String hash, NotificationContent notificationContent) throws IOException {
        JsonGenerator jsonGenerator = null;
        try {
            String notificationContentJson = JsonHandler.convertToJson(notificationContent);
            JsonFactory jFactory = new JsonFactory();
            StringWriter writer = new StringWriter();
            jsonGenerator = jFactory.createGenerator(writer);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("hash", hash);
            jsonGenerator.writeStringField("notificationContent", notificationContentJson);
            jsonGenerator.writeStringField("timestamp", LocalDateTime.now().toString());
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            return writer.toString();
        } catch (Exception e) {
            logger.error("Recording of HASH in FileSystem encountered and error: " + e.getMessage());
        } finally {
            if (null != jsonGenerator)
                jsonGenerator.close();
        }
        return null;
    }


    private String getRecordFromFile(Path path) throws Exception {
        Optional<String> data = Files.readAllLines(path).stream().findFirst();
        if (data.isPresent())
            return data.get();
        else
            throw new Exception("Data not retrieved in Files!");
    }

    public String getClearingHouseHashDir() {
        return clearingHouseHashDir;
    }

    public void setClearingHouseHashDir(String clearingHouseHashDir) {
        this.clearingHouseHashDir = clearingHouseHashDir;
    }

    public Path getPath(String hash) throws Exception {
        if (StringUtils.isBlank(getClearingHouseHashDir())) {
            logger.error("No Dir PATH to record hash was found! or application.clearingHouseHashDir prop is empty");
            throw new Exception("No Dir PATH to record hash was found! or application.clearingHouseHashDir prop is empty");
        }
        return Paths.get(this.clearingHouseHashDir + File.separator + hash);
    }

}
