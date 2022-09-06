package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class HashFileServiceImplTest {

	@InjectMocks
	private HashFileServiceImpl hashFileService;

	private String clearingHouseHashDir;
	
	private String payload;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		clearingHouseHashDir = "some/directory";
		payload = "Some payload to hash";
		ReflectionTestUtils.setField(hashFileService, "clearingHouseHashDir", clearingHouseHashDir);
	}

	@Test
	public void hashSuccessful() {
		String hashedValue = hashFileService.hash(payload);
		assertNotNull(hashedValue);
		assertTrue(hashedValue.startsWith("fc56de6811d1"));
	}

	@Test
	public void hashFailed() {
		String hashedValue = hashFileService.hash(null);
		assertNull(hashedValue);
	}
	
	@Test
	public void recordHash() {
		hashFileService.recordHash(clearingHouseHashDir, payload, null);
	}

}
