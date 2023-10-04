package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.eng.idsa.businesslogic.util.JwTokenUtil;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DapsOrbiterServiceImplTest {
	private String dapsUrl = "https://mockaddress.com";

	@Mock
	private OkHttpClient client;

	@Mock
	private DapsOrbiterProvider dapsOrbiterProvider;

	@InjectMocks
	private DapsOrbiterServiceImpl dapsOrbiterServiceImpl;

	private Request requestDaps;

	private String JSONAnswer;

	private String stringAnswer;

	@Mock
	private Call call;
	@Mock
	private Call callValidate;

	private Response response;

	private String jws;

	@BeforeEach
	public void setup() throws IOException, GeneralSecurityException {
		MockitoAnnotations.openMocks(this);

		jws = "ABC";
		when(dapsOrbiterProvider.provideJWS()).thenReturn(jws);
		JSONAnswer = "{\"response\":\"mockToken\"}";
		stringAnswer = "Token can not be retrieved";

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("grant_type", "client_credentials");
		jsonObject.put("client_assertion_type", "jwt-bearer");
		jsonObject.put("client_assertion", jws);
		jsonObject.put("scope", "all");
		Gson gson = new GsonBuilder().create();
		String jsonString = gson.toJson(jsonObject);
		RequestBody formBody = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8")); // new

		requestDaps = new Request.Builder().url(dapsUrl).header("Host", "ecc-receiver")
				.header("accept", "application/json").header("Content-Type", "application/json").post(formBody).build();
		when(client.newCall(any(Request.class))).thenReturn(call);
		ReflectionTestUtils.setField(dapsOrbiterServiceImpl, "dapsUrl", dapsUrl);
	}

	@Test
	public void testSuccesfulTokenResponse() throws IOException {
		response = new Response.Builder()
					.request(requestDaps)
					.protocol(Protocol.HTTP_1_1)
					.message("ABC")
					.body(ResponseBody.create(JSONAnswer, MediaType.get("application/json; charset=utf-8")))
					.code(200)
				.build();

		when(call.execute()).thenReturn(response);
		assertEquals("mockToken", dapsOrbiterServiceImpl.getJwTokenInternal());
	}

	@Test
	public void testResponseStringInsteadOfJson() throws IOException {
		response = new Response.Builder()
						.request(requestDaps)
						.protocol(Protocol.HTTP_1_1)
						.message("ABC")
						.body(ResponseBody.create(stringAnswer, MediaType.get("text/plain")))
						.code(200)
					.build();
		when(call.execute()).thenReturn(response);
		assertEquals(null, dapsOrbiterServiceImpl.getJwtToken());
	}

	@Test
	public void testValidateTokenSuccess() throws IOException, ParseException {
		String mockToken = JwTokenUtil.generateToken(false);

		 String tokenResponseValid = "{ \"response\": \"true\",\r\n" + 
		      		" \"description\": \"Token successfully validated\"\r\n" + 
		      		" }";
		mockValidateCall(mockToken, tokenResponseValid);
		boolean valid = dapsOrbiterServiceImpl.validateToken(mockToken);
		assertTrue(valid);
	}
	
	@Test
	public void testValidateTokenFailed() throws IOException, ParseException {
		String mockToken = JwTokenUtil.generateToken(false);

		 String tokenResponseValid = "{ \"response\": \"false\",\r\n" + 
		      		" \"description\": \"Token failed to validate\"\r\n" + 
		      		" }";
		mockValidateCall(mockToken, tokenResponseValid);
		boolean valid = dapsOrbiterServiceImpl.validateToken(mockToken);
		assertFalse(valid);
	}
	
	@Test
	public void testValidateTokenExpired() throws IOException, ParseException {
		String mockToken = JwTokenUtil.generateToken(true);

		 String tokenResponseValid = "{ \"response\": \"true\",\r\n" + 
		      		" \"description\": \"Token successfully validated\"\r\n" + 
		      		" }";
		 
		mockValidateCall(mockToken, tokenResponseValid);
		boolean valid = dapsOrbiterServiceImpl.validateToken(mockToken);
		assertFalse(valid);
	}

	private void mockValidateCall(String mockToken, String tokenResponseValid) throws IOException {
		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("token", mockToken);
		Gson gson = new GsonBuilder().create();
		String jsonStringValidate = gson.toJson(jsonObject);
		RequestBody formBodyValidate = RequestBody.create(jsonStringValidate, MediaType.parse("application/json; charset=utf-8"));
      
		// @formatter:off
		Request requestDapsValidate = new Request.Builder()
			.url(dapsUrl + "/validate")
			.header("Host", "ecc-receiver")
			.header("accept", "application/json")
			.header("Content-Type", "application/json")
			.post(formBodyValidate)
			.build();
		// @formatter:on
      
      Response responseValidate =  new Response.Builder()
				.request(requestDapsValidate)
				.protocol(Protocol.HTTP_1_1)
				.message("ABC_Validate")
				.body(ResponseBody.create(tokenResponseValid, MediaType.get("application/json; charset=utf-8")))
				.code(200)
				.build();

      	when(call.execute()).thenReturn(responseValidate);
	}
}
