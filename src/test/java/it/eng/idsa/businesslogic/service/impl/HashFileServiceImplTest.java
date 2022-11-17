package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.configuration.ClearingHouseConfiguration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HashFileServiceImplTest {

	@InjectMocks
	private HashFileServiceImpl hashFileService;
	
	@Mock
	private ClearingHouseConfiguration clearingHouseConfiguration;
	
	private String payload;
	
	private String hash;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(clearingHouseConfiguration.getHashDir()).thenReturn("./");
		payload = "Some payload to hash";
		hash = "fc56de6811d1";
	}
	
	@AfterEach
	public void teardown() throws IOException {
		// to delete the file that the test made
		if (Files.exists(Path.of("./", hash)))
		Files.delete(Path.of("./", hash));
	}

	@Test
	public void hashSuccessful() {
		String hashedValue = hashFileService.hash(payload);
		assertNotNull(hashedValue);
		assertTrue(hashedValue.startsWith(hash));
	}

	@Test
	public void hashFailed() {
		String hashedValue = hashFileService.hash(null);
		assertNull(hashedValue);
	}
	
	
	@Test
	public void getContent() throws Exception {
		// create file for testing
		Path path = Path.of("./", hash);
		if (Files.notExists(path))
			Files.createFile(path);
		Files.write(path, hash.getBytes(), StandardOpenOption.CREATE);
		assertDoesNotThrow(() -> hashFileService.getContent(hash));
	}

	@Test
	public void getContent_noSuchFile() throws Exception {
		assertThrows(NoSuchFileException.class, () ->hashFileService.getContent(hash));
	}
	
	@Test
	public void getContent_noData() throws Exception {
		// create file for testing
		Path path = Path.of("./", hash);
		if (Files.notExists(path))
			Files.createFile(path);
		Files.write(path, "".getBytes(), StandardOpenOption.CREATE);
		assertThrows(Exception.class, () ->hashFileService.getContent(hash));
	}

}
