package it.eng.idsa.businesslogic.camel_interceptor_ucapp.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.camel_interceptor_ucapp.exception.PolicyDeniedException;
import it.eng.idsa.businesslogic.camel_interceptor_ucapp.model.IdsUseObject;
import retrofit2.Call;
import retrofit2.Response;

@Service
public class UcService {
	
	private UcRestCallService ucRestCallService;

	@Autowired
	public UcService(UcRestCallService ucRestCallService) {
	this.ucRestCallService = ucRestCallService;
	}
	
	public Object enforceUsageControl(IdsUseObject idsUseObject) {
		Call<Object> callSync = ucRestCallService.enforceUsageControl(idsUseObject);
		try {
			Response<Object> response = callSync.execute();
			if (!response.isSuccessful()) {
				throw new PolicyDeniedException(response);
			}
			return response.body(); 
		}catch (IOException ioe) {
			return "";
		}
	}


}
