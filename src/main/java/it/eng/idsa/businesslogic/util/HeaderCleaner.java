package it.eng.idsa.businesslogic.util;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Arrays;

@Component
public class HeaderCleaner {
	private static final Logger logger = LogManager.getLogger(HeaderCleaner.class);

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
