package it.eng.idsa.businesslogic.util;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import it.eng.idsa.businesslogic.util.exception.OCSPValidationException;

public class OCSPValidationTest {

	@Test
	@Disabled("Should bot be run in build")
	public void testOCSP() throws MalformedURLException, OCSPValidationException {
		boolean validCert = OCSPValidation.checkOCSPCerificate("https://daps.aisec.fraunhofer.de", 
				OCSPValidation.OCSP_STATUS.good);
		
		System.out.println("Status of cert validation: " + validCert);
	}
}
