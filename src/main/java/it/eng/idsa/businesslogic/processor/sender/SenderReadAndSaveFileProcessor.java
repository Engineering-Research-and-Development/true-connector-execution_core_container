package it.eng.idsa.businesslogic.processor.sender;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.camel.Processor;


@Component
public class SenderReadAndSaveFileProcessor implements Processor {
	
	@Value("${application.sourceFileName}")
	private String destinationFileName;
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try {
			byte[] bytes = exchange.getMessage().getBody(byte[].class);
		
			try (OutputStream stream = new FileOutputStream(destinationFileName)) {				
			    stream.write(bytes);
			}
			
		} catch (Exception e) {
			
			
		}
		
	}
	
}

