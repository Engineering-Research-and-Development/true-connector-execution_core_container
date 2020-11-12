package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;

import de.fraunhofer.iais.eis.Message;

public class ClearingHouseServiceImplTest {
	
	ClearingHouseServiceImpl clearingHouseServiceImpl = new ClearingHouseServiceImpl();
	
	MultipartMessageServiceImpl multipartMessageServiceImpl = new MultipartMessageServiceImpl();
	
	Message message;
	
	String payload;
	
	String directory = "./src/test/resources/ClearingHouseServiceImplTest/";
	
	
	@Before
	public void init() {
		String clearingHouseHeader = null;
		String clearingHousePayload = null;
		try {
			clearingHouseHeader = new String(Files.readAllBytes(Paths.get(directory+"clearingHouseHeader.txt")));
			clearingHousePayload = new String(Files.readAllBytes(Paths.get(directory+"clearingHousePayload.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = multipartMessageServiceImpl.getMessage(clearingHouseHeader);
		payload = clearingHousePayload;
	}
	
	/*
	 * @Test public void testRegisterTransaction () {
	 * assertFalse(clearingHouseServiceImpl.registerTransaction(message, payload));
	 * }
	 */
	
	

}
