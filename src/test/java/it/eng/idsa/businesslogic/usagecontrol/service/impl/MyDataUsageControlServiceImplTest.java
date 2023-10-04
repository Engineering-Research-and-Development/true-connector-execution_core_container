package it.eng.idsa.businesslogic.usagecontrol.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import it.eng.idsa.businesslogic.service.CommunicationService;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsUseObject;
import it.eng.idsa.businesslogic.usagecontrol.model.Meta;
import it.eng.idsa.businesslogic.usagecontrol.model.TargetArtifact;
import it.eng.idsa.businesslogic.usagecontrol.model.UsageControlObject;
import it.eng.idsa.businesslogic.usagecontrol.service.UcRestCallService;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.multipart.util.UtilMessageService;
import retrofit2.Call;
import retrofit2.Response;

public class MyDataUsageControlServiceImplTest {

	@InjectMocks
	private MyDataUsageControlServiceImpl myDataUsageControlServiceImpl;
	
	@Mock
	private UcRestCallService ucRestCallService;
	
	@Mock
	private CommunicationService communicationService;
	
	@Mock
	private Call<Object> call;
	
	@Mock
	private Response<Object> response;
	
	@Mock
	private Gson gson;
	
	@Mock
	JsonObject jsonObject;
	
	private UsageControlObject ucObj;
	
	@Mock
	private JsonElement jsonElement;
	
	private String myDataURL = "http://mydata.com";
	
	String payload = "{\"some\":\"json\"}";
	
	@BeforeEach
	public void init () {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(myDataUsageControlServiceImpl, "usageControlDataAppURL", myDataURL);
	}
	
	@Test
	public void testEnforceUsageControl() throws Exception {
		
		URI caURI = URI.create("http://someContractAgreement.com/1");
		
		URI provider = URI.create("http://provider.com");
		URI consumer = URI.create("http://consumer.com");
		URI targetArtifactURI = URI.create("http://artifact.com");
		
		TargetArtifact targetArtifact = new TargetArtifact(ZonedDateTime.now(), targetArtifactURI);
		
		Meta meta = new Meta(provider, consumer, targetArtifact);
		
		ucObj = new UsageControlObject(meta, jsonElement);
		
		LinkedTreeMap<Object, Object> treeMap = new LinkedTreeMap<>();
		
		when(gson.fromJson(anyString(), any())).thenReturn(jsonElement);
		when(gson.fromJson(any(JsonElement.class), any())).thenReturn(ucObj);
		when(gson.toJsonTree(treeMap)).thenReturn(jsonElement);
		when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
		when(jsonObject.get(MessagePart.PAYLOAD)).thenReturn(jsonElement);
		when(jsonElement.getAsString()).thenReturn("Usage allowed");
		when(ucRestCallService.enforceUsageControl(any(IdsUseObject.class))).thenReturn(call);
		when(call.execute()).thenReturn(response);
		when(response.isSuccessful()).thenReturn(true);
		when(response.body()).thenReturn(treeMap);
		
		String response = myDataUsageControlServiceImpl.enforceUsageControl(caURI,targetArtifactURI, payload);
		
		assertEquals("Usage allowed", response);
	}
	
	@Test
	public void testCreateUsageControlObject() {
		
		when(gson.fromJson(anyString(), any())).thenReturn(jsonElement);
		when(gson.toJson(any(), any(Type.class))).thenReturn(payload);
		
		String response = myDataUsageControlServiceImpl.createUsageControlObject(UtilMessageService.getArtifactRequestMessage(),
				UtilMessageService.getArtifactResponseMessage(), payload);
		
		assertEquals(payload, response);
	}
	
	@Test
	public void testUploadPolicy( ) {
		
		when(communicationService.sendDataAsJson(myDataURL + "policy/usage/odrl", payload, "application/ld+json;charset=UTF-8")).thenReturn("Policy uploaded");
		
		String response = myDataUsageControlServiceImpl.uploadPolicy(payload);
		
		assertEquals("Policy uploaded", response);

	}
}
