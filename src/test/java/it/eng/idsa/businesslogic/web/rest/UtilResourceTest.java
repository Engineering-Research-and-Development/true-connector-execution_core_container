package it.eng.idsa.businesslogic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.info.BuildProperties;

public class UtilResourceTest {

	@InjectMocks
	private UtilResource utilResource;
	
	@Mock
	private BuildProperties buildProperties;
	
	private String version = "1.0";

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void getVersion() {
		when(buildProperties.getVersion()).thenReturn(version);
		String ver = utilResource.getVersion();
		
		verify(buildProperties).getVersion();
		assertEquals(version, ver);
	}
}
