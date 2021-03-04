package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import it.eng.idsa.businesslogic.service.DapsService;

public class DapsTokenProviderServiceImplTest {

	@Mock
	private DapsService dapsService;

	@InjectMocks
	private DapsTokenProviderServiceImpl dapsTokenProviderServiceImpl;

	private String dapsToken;

	@BeforeEach
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", true);
		dapsToken = generateToken(1000*60*60);
		when(dapsService.getJwtToken()).thenReturn(dapsToken);

	}

	@Test
	public void fetchNewTokenIfCachedTokenIsNullTest() {
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();

	}

	@Test
	public void tokenExpiredAndFetchedNewTokenTest() {
		long expired = 1613643693000L;
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", expired);
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();

	}
	
	@Test
	public void fetchNewTokenWhenExpiredTest() {
		when(dapsService.getJwtToken()).thenReturn("ABC");
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", 1613643693000L);
		assertNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();

	}

	@Test
	public void failedToFetchNewTokenWhenExpiredTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", 2613643693000L);
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService, times(0)).getJwtToken();
	}

	@Test
	public void tokenCachingOffTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", false);
		assertNotNull(dapsTokenProviderServiceImpl.provideToken());
		verify(dapsService).getJwtToken();
	}

	private String generateToken(long expiration) throws ParseException {
		String id = "";
		String issuer = "demo_token";
		String subject = "demo token subject";
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer);

		long expMillis = nowMillis + expiration;
		Date exp = new Date(expMillis);
		builder.setExpiration(exp);

		return builder.compact();
	}

}
