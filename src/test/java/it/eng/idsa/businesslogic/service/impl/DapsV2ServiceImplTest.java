package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.idsa.businesslogic.util.JwTokenUtil;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DapsV2ServiceImplTest {
	private String dapsUrl = "https://mockaddress.dapsv2.com";
	private String jws = "jws.Mock";

	@InjectMocks
	private DapsV2ServiceImpl dapsV2Service;
	
	@Mock
    private DapsUtilityProvider dapsUtilityProvider;
	@Mock
	private TransportCertsManager transportCertsManager;
	@Mock
	private OkHttpClient client;
	@Mock
	private Call call;
	@Mock
	private Algorithm algorithm;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(dapsUtilityProvider.getDapsV2Jws()).thenReturn(jws);
		when(transportCertsManager.getConnectorTransportCertsSha()).thenReturn("TOKEN_HASHED_VALUE");
		ReflectionTestUtils.setField(dapsV2Service, "dapsUrl", dapsUrl);
	}
	
	@Test
	public void getTokenSuccess() throws IOException, ParseException {
		Response response =  new Response.Builder()
				.request(mockRequest())
				.protocol(Protocol.HTTP_1_1)
				.message("ABC")
				.body(ResponseBody.create(mockJsonResponse(), MediaType.get("application/json; charset=utf-8")))
				.code(200)
				.build();
		
		when(client.newCall(any(Request.class))).thenReturn(call);
		when(call.execute()).thenReturn(response);//.thenReturn(responseValidate);

		String token = dapsV2Service.getJwTokenInternal();
		assertNotNull(token);
	}
	
	@Test
	public void getTokenFailed() throws IOException {
		String noTokenPresent = "{\r\n" + 
				"	\"expires_in\": 3600,\r\n" + 
				"	\"token_type\": \"bearer\",\r\n" + 
				"	\"scope\": \"idsc:IDS_CONNECTOR_ATTRIBUTES_ALL\"\r\n" + 
				"}";
		Response response =  new Response.Builder()
				.request(mockRequest())
				.protocol(Protocol.HTTP_1_1)
				.message("ABC")
				.body(ResponseBody.create(noTokenPresent, MediaType.get("application/json; charset=utf-8")))
				.code(200)
				.build();
		
		when(client.newCall(any(Request.class))).thenReturn(call);
		when(call.execute()).thenReturn(response);
		
		String token = dapsV2Service.getJwTokenInternal();
		assertNull(token);
	}
	
	@Test
	public void validateTokenSuccess() throws ParseException {
		String tokenValue = JwTokenUtil.generateToken(false);
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doNothing().when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertTrue(valid);
	}
	
	@Test
	public void validateTokenButExpired() throws ParseException {
		String tokenValue = JwTokenUtil.generateToken(true);
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doNothing().when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertFalse(valid);
	}
	
	@Test
	public void validateTokenFailed() throws ParseException {
		String tokenValue = JwTokenUtil.generateToken(false);
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doThrow(SignatureVerificationException.class).when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertFalse(valid);
	}
	
	@Test
	public void validateEmptyTokenFailed() throws ParseException {
		String tokenValue = "";
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doThrow(JWTDecodeException.class).when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertFalse(valid);
	}
	
	@Test
	public void validateBlankTokenFailed() throws ParseException {
		String tokenValue = " ";
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doThrow(JWTDecodeException.class).when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertFalse(valid);
	}

	@Test
	public void validateWrongFormatTokenFailed() throws ParseException {
		String tokenValue = "ABC";
		
		when(dapsUtilityProvider.provideAlgorithm(tokenValue)).thenReturn(algorithm);
		doThrow(JWTDecodeException.class).when(algorithm).verify(any(DecodedJWT.class));
		
		boolean valid = dapsV2Service.validateToken(tokenValue);
		assertFalse(valid);
	}
	
	private Request mockRequest() {
		 RequestBody formBody =
             new FormBody.Builder()
                     .add("grant_type", "client_credentials")
                     .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                     .add("client_assertion", jws)
                     .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                     .build();
		 return new Request.Builder().url(dapsUrl).post(formBody).build();
	}
	
	private String mockJsonResponse() throws ParseException, JsonProcessingException {
		Map<String, String> resp = new HashMap<>();
		
		resp.put("access_token", JwTokenUtil.generateToken(false));
		resp.put("expires_in", "3600");
		resp.put("token_type", "bearer");
		resp.put("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL");
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resp);
	}
}
