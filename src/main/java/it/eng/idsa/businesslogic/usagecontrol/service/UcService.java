package it.eng.idsa.businesslogic.usagecontrol.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import it.eng.idsa.businesslogic.usagecontrol.exception.PolicyDeniedException;
import it.eng.idsa.businesslogic.usagecontrol.model.IdsUseObject;
import retrofit2.Call;
import retrofit2.Response;

@Service
@ConditionalOnProperty(name = "application.isEnabledUsageControl", havingValue = "true", matchIfMissing = false)
public class UcService {
	
	private static final Logger logger = LoggerFactory.getLogger(UcService.class);
	
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
			logger.error("Uc Service has failed: {}", ioe);
			return "";
		}
	}


}
