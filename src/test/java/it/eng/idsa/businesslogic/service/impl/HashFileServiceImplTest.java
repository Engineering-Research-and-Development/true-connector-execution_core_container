package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HashFileServiceImplTest {

	private HashFileServiceImpl hashFileService;

	@BeforeEach
	public void setup() {
		hashFileService = new HashFileServiceImpl("clearingHouseHashDir");
	}

	@Test
	public void hashSuccessful() {
		String payload = "Some payload to hash";
		String hashedValue = hashFileService.hash(payload);
		assertNotNull(hashedValue);
		assertTrue(hashedValue.startsWith("fc56de6811d1"));
	}

	@Test
	public void hashFailed() {
		String hashedValue = hashFileService.hash(null);
		assertNull(hashedValue);
	}

}
