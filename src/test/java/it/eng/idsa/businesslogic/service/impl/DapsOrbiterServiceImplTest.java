package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
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

import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
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
	
	private RejectionMessageService rejectionMessageService;
	
	@BeforeEach
	public void setup() throws  IOException, GeneralSecurityException {
		MockitoAnnotations.initMocks(this);
		
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
        RequestBody formBody = RequestBody
        		.create(MediaType.get("application/json; charset=utf-8"), jsonString); // new

        requestDaps = new Request.Builder()
        		.url(dapsUrl)
        		.header("Host", "ecc-receiver")
				.header("accept", "application/json")
				.header("Content-Type", "application/json")
                .post(formBody)
                .build();
        when(client.newCall(any(Request.class))).thenReturn(call);
		ReflectionTestUtils.setField(dapsOrbiterServiceImpl, "dapsUrl", dapsUrl);
	}
	
	@Test
	public void testSuccesfulTokenResponse() throws IOException {
		response =  new Response.Builder()
				.request(requestDaps)
				.protocol(Protocol.HTTP_1_1)
				.message("ABC")
				.body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), JSONAnswer))
				.code(200)
				.build();
		
		Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("token", "mockToken");
        Gson gson = new GsonBuilder().create();
        String jsonStringValidate = gson.toJson(jsonObject);
        RequestBody formBodyValidate = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStringValidate);
        
        Request requestDapsValidate = new Request.Builder()
				.url(dapsUrl + "/validate")
				.header("Host", "ecc-receiver")
				.header("accept", "application/json")
				.header("Content-Type", "application/json")
				.post(formBodyValidate)
				.build();
		// @formatter:on
        
        String tokenResponseValid = "{ \"response\": \"true\",\r\n" + 
        		" \"description\": \"Token successfully validated\"\r\n" + 
        		" }";
        Response responseValidate =  new Response.Builder()
				.request(requestDapsValidate)
				.protocol(Protocol.HTTP_1_1)
				.message("ABC_Validate")
				.body(ResponseBody.create(MediaType.get("application/json; charset=utf-8"), tokenResponseValid))
				.code(200)
				.build();
		
		when(call.execute()).thenReturn(response).thenReturn(responseValidate);
		assertEquals("mockToken", dapsOrbiterServiceImpl.getJwtToken());
	}
	
	@Test
	public void testResponseStringInsteadOfJson() throws IOException {
		rejectionMessageService = new RejectionMessageServiceImpl();
		ReflectionTestUtils.setField(dapsOrbiterServiceImpl, "rejectionMessageService", 
				rejectionMessageService, RejectionMessageService.class);
		
		response =  new Response.Builder()
				.request(requestDaps)
				.protocol(Protocol.HTTP_1_1)
				.message("ABC")
				.body(ResponseBody.create(MediaType.get("text/plain"), stringAnswer))
				.code(200).build();
		when(call.execute()).thenReturn(response);

		assertThrows(ExceptionForProcessor.class,
	            ()->{
	            	dapsOrbiterServiceImpl.getJwtToken();
	            });
	}
}
