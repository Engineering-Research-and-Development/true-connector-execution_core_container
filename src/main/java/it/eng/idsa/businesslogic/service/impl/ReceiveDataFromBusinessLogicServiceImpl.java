package it.eng.idsa.businesslogic.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;

import it.eng.idsa.businesslogic.service.ReceiveDataFromBusinessLogicService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

@Service
public class ReceiveDataFromBusinessLogicServiceImpl implements ReceiveDataFromBusinessLogicService {

	private static final Logger logger = LogManager.getLogger(ReceiveDataFromBusinessLogicServiceImpl.class);

	@Override
	public MultipartMessage receiveMessageBinary(Exchange exchange) {
		MultipartMessage multipartMessage = null;
		Map<String, String> messageParts = null;

//		try {
//			messageParts = httpHeadersToString(exchange.getIn().getHeaders());
////			napravi formater za poruke koji formatira na osnovu content-type-a
////			messageParts.replace("header" ,messageFormater(messageParts.get("header"), messageParts.get(key)));
//			switch ((String)headesParts.get("Content-Type-header")) {
//            case "application/json; charset=utf-8":
//            //case "Content-Type: application/json; charset=utf-8":
//                DataHandler dtHeader = (DataHandler) exchange.getIn().getHeader("header");
//                header = IOUtils.toString(dtHeader.getInputStream());
//                break;
//
//
//
//            default:
//                header = exchange.getIn().getHeader("header").toString();
//                break;
//            }
//            
//            switch ((String)headesParts.get("Content-Type-payload")) {
//            case "application/json; charset=utf-8":
//                DataHandler dtPayload = (DataHandler) exchange.getIn().getHeader("payload");
//                payload = IOUtils.toString(dtPayload.getInputStream());
//                break;
//
//
//
//            default:
//                payload = exchange.getIn().getHeader("payload").toString();
//                break;
//            }
//			multipartMessage = new MultipartMessageBuilder()
//					.withHttpHeader(messageParts)
////					.withHeaderHeader(Splitter.))
//					.withHeaderContent(messageParts.get("header"))
//					.withPayloadContent(messageParts.get("payload"))
//					.build();
//		} catch (NullPointerException e) {
//			logger.error("Received message or http headers are empty", e);
//		} catch (Exception e) {
//			logger.error("Failed to receive message", e);
//		}
//		return multipartMessage;
//	}
//
//	private Map<String, String> httpHeadersToString(Map<String, Object> httpHeaders) {
//		Map<String, String> httpHeadersString = new HashMap<>();
//		httpHeaders.forEach((key, value) -> {
//			if (value != null) {
//				httpHeadersString.put(key, value.toString());
//			} else {
//				httpHeadersString.put(key, null);
//
//			}
//		});
		return null;
	}

}
