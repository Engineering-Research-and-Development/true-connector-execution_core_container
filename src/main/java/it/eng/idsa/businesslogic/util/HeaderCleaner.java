package it.eng.idsa.businesslogic.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeaderCleaner {
	private static final Logger logger = LoggerFactory.getLogger(HeaderCleaner.class);

	private String headersForRemoval;
	
	public HeaderCleaner(@Value("${application.technicalHeaders}") String headersForRemoval) {
		this.headersForRemoval = headersForRemoval;
	}

	public void removeTechnicalHeaders(Map<String, Object> headers) {

		List<String> technicalHeadersList = Arrays.stream(headersForRemoval.split(","))
			    .map(String::trim)
			    .collect(Collectors.toList());
				
		for (String technicalHeader : technicalHeadersList) {
			if (headers.containsKey(technicalHeader)) {
				logger.debug("==============Technical header=========================="+ technicalHeader);
				headers.remove(technicalHeader);
			}
		}
	}
	
}
