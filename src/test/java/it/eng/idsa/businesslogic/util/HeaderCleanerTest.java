package it.eng.idsa.businesslogic.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class HeaderCleanerTest {
	
	private HeaderCleaner headerCleaner = new HeaderCleaner("FOR_REMOVE, ANOTHER_FOR_REMOVE");
	
	@Test
	public void removeHeaders() {
		Map<String, Object> headers = new HashMap<>();
		headers.put("FOR_REMOVE", "ABC");
		headers.put("DO_NOT_REMOVE", "test");
		headers.put("ANOTHER_FOR_REMOVE", "XYZ");
		headers.put("Content-Type", "application/json");
		
		headerCleaner.removeTechnicalHeaders(headers);
		
		assertEquals(2, headers.size());
		assertTrue(headers.containsKey("DO_NOT_REMOVE"));
		assertTrue(headers.containsKey("Content-Type"));
	}

}
