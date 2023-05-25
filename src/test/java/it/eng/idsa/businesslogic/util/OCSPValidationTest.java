package it.eng.idsa.businesslogic.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import it.eng.idsa.businesslogic.util.exception.OCSPValidationException;

public class OCSPValidationTest {

	@Test
	public void testOCSP() throws MalformedURLException, OCSPValidationException {
		boolean validCert = OCSPValidation.checkOCSPCerificate(new URL("https://daps.aisec.fraunhofer.de"), 
				OCSPValidation.OCSP_STATUS.good.name());
		
		System.out.println("Status of cert validation: " + validCert);
	}
}
