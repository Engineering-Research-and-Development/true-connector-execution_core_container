package it.eng.idsa.businesslogic.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;

import it.eng.idsa.businesslogic.exception.ProcessorException;
import it.eng.idsa.businesslogic.processor.MultiPartMessageProcessor;
import it.eng.idsa.businesslogic.processor.SendDataToDestinationProcessor;
import it.eng.idsa.businesslogic.processor.SendTransactionToCHProcessor;
import it.eng.idsa.businesslogic.processor.ValidateTokenProcessor;

@Component
public class CamelRoute extends RouteBuilder {
	
	private static final Logger logger = LogManager.getLogger(SendDataToDestinationProcessor.class);
	
	@Autowired
	ValidateTokenProcessor validateTokenProcessor;
	
	@Autowired
	MultiPartMessageProcessor multiPartMessageProcessor;
	
	@Autowired
	SendDataToDestinationProcessor sendDataToDestinationProcessor;
	
	@Autowired
	SendTransactionToCHProcessor sendTransactionToCHProcessor;
	
	@Override
	public void configure() throws Exception {

		onException(ProcessorException.class,RuntimeException.class)
			.log(LoggingLevel.ERROR, "ProcessorException in the route ${body}");

// Camel SSL - Endpoint: B		
//		from("jetty://https4://localhost:8888/incoming-data-channel/receivedMessage")
//			.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
//			.log(">>> ${body}")
//			.process(multiPartMessageProcessor)
//			.process(validateTokenProcessor)
//			.process(sendDataToDestinationProcessor)
//			.process(sendTransactionToCHProcessor);
		
	}
}
