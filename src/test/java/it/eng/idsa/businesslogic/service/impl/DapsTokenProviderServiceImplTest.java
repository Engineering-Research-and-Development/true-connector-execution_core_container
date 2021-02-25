package it.eng.idsa.businesslogic.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import it.eng.idsa.businesslogic.service.DapsService;

public class DapsTokenProviderServiceImplTest {

	@Mock
	private DapsService dapsService;

	@InjectMocks
	private DapsTokenProviderServiceImpl dapsTokenProviderServiceImpl;

	private String dapsToken;

	@BeforeEach
	public void setup() throws ConstraintViolationException, URISyntaxException, IOException, InvalidKeySpecException,
			GeneralSecurityException {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", true);
		dapsToken = "eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfQ09OTkVDVE9SX1NFQ1VSSVRZX1BST0ZJTEUiLCJyZWZlcnJpbmdDb25uZWN0b3IiOiJodHRwOi8vaXNzdGJyb2tlci5kZW1vIiwiQHR5cGUiOiJpZHM6RGF0UGF5bG9hZCIsIkBjb250ZXh0IjoiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvbnRleHRzL2NvbnRleHQuanNvbmxkIiwidHJhbnNwb3J0Q2VydHNTaGEyNTYiOiIwZWU3M2RjMGViY2MwOTdhODEzMzRmNTRkNzkyMTc2NWZiMTNiOTFhN2EzYWE2YWMxZGVmYzc5Zjk1MzZkOTQzIiwic2NvcGVzIjpbImlkc2M6SURTX0NPTk5FQ1RPUl9BVFRSSUJVVEVTX0FMTCJdLCJhdWQiOiJpZHNjOklEU19DT05ORUNUT1JTX0FMTCIsImlzcyI6Imh0dHBzOi8vZGFwcy5haXNlYy5mcmF1bmhvZmVyLmRlIiwic3ViIjoiMTc6N0I6RUQ6MTg6NzM6RUI6RDA6NDc6NUM6QzM6MjU6NDk6NDc6MDQ6M0Q6QTI6OEI6NzI6ODY6QkY6a2V5aWQ6Q0I6OEM6Qzc6QjY6ODU6Nzk6QTg6MjM6QTY6Q0I6MTU6QUI6MTc6NTA6MkY6RTY6NjU6NDM6NUQ6RTgiLCJuYmYiOjE2MTM2NDAwOTMsImlhdCI6MTYxMzY0MDA5MywianRpIjoiTVRJeE1qZzVORGMxTlRNek1USTVOVGs1TURjPSIsImV4cCI6MTYxMzY0MzY5M30.LNCMKRQDyMFieErmP2dq40Ub_dCxeb0drlzcyoBmXzlbahyiAzKCX3SLLMUwRHchybnWMY6qTaoBoVFbC9bBUVgg252FZw04yDMJPyHsrzBPuKepsUojycqLdc1pcWdctodeH_W4BpzByKKj9GqALLM9QIzxpmjoXq8DbVVmXXnJwsRTKvHfQHXrgA-Ws5ZXwDFJk5VHx73e537cxTFcVIfAAr5gBewtz03t1oy8IqUK76pVAN_VLVtwUgMBUd54CrCFFvpvNkZo6HTj5I_pPNkLAiV0ioPaRXJ68lLoRvPyWIBmVcALJk1A-OQYBErzWXpecCgybstJ75Sayvn01Q";
		when(dapsService.getJwtToken()).thenReturn(dapsToken);
		
	}
	
	@Test
	public void tokenNullTest() {
		String token = dapsTokenProviderServiceImpl.provideToken();
		assertNotNull(token);
		verify(dapsService).getJwtToken();


	}

	@Test
	public void tokenExpiredTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", 1613643693000L);
		String token = dapsTokenProviderServiceImpl.provideToken();
		assertNotNull(token);
		verify(dapsService).getJwtToken();


	}

	@Test
	public void tokenNotExpiredTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", 2613643693000L);
		String token = dapsTokenProviderServiceImpl.provideToken();
		assertNotNull(token);
		verify(dapsService, times(0)).getJwtToken();
	}
	
	@Test
	public void tokenCachingOffTest() {
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "tokenCaching", false);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "cachedToken", dapsToken);
		ReflectionTestUtils.setField(dapsTokenProviderServiceImpl, "expirationTime", 2613643693000L);
		String token = dapsTokenProviderServiceImpl.provideToken();
		assertNotNull(token);
		verify(dapsService).getJwtToken();
	}

}
