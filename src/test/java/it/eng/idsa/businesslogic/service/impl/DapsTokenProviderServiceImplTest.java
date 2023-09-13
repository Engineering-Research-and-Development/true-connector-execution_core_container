package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.util.JwTokenUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

public class DapsTokenProviderServiceImplTest {

	@Mock
	private DapsService dapsService;

	@InjectMocks
	private DapsTokenProviderServiceImpl dapsTokenProviderServiceImpl;

	private String dapsToken;

	@BeforeEach
	public void setup() throws ParseException {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", true);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "useDaps", true);
		dapsToken = JwTokenUtil.generateToken(false);
		when(dapsService.getJwtToken()).thenReturn(dapsToken);
	}

	@Test
	public void fetchNewTokenIfCachedTokenIsNullTest() {
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();
	}

	@Test
	public void tokenExpiredAndFetchedNewTokenTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", JwTokenUtil.isExpired(true));
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();
	}
	
	@Test
	public void fetchNewTokenWhenExpiredTest() {
		when(dapsService.getJwtToken()).thenReturn("ABC");
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", JwTokenUtil.isExpired(true));
		assertNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();
	}

	@Test
	public void failedToFetchNewTokenWhenExpiredTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", JwTokenUtil.isExpired(false));
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService, times(0)).getJwtToken();
	}

	@Test
	public void tokenCachingOffTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", false);
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();
	}

	@Test
	public void fetchTokenDapsDisabled() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "useDaps", false);
		assertEquals(UtilMessageService.TOKEN_VALUE, dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService, times(0)).getJwtToken();
	}
}
