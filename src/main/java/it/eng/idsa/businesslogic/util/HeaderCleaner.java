package it.eng.idsa.businesslogic.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderCleaner {
	private static final Logger logger = LoggerFactory.getLogger(HeaderCleaner.class);

	@Value("${application.technicalHeaders}")
	public void setHeaders(String headers) {
		technicalHeaders = headers;
	}

	private String technicalHeaders;

	public void removeTechnicalHeaders(Map<String, Object> headers) {

		List<String> technicalHeadersList = Arrays.asList(technicalHeaders.split(","));

		for (String technicalHeader : technicalHeadersList) {
			if (headers.containsKey(technicalHeader)) {
				logger.debug("==============Technical header=========================="+ technicalHeader);
				headers.remove(technicalHeader);
			}
		}
	}
	
}
