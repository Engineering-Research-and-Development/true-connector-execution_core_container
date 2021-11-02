package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.businesslogic.processor.exception.ExceptionForProcessor;
import it.eng.idsa.businesslogic.service.DapsTokenProviderService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.multipart.util.UtilMessageService;

public class RejectionMessageServiceImplTest {
	
	private static final Logger logger = LoggerFactory.getLogger(RejectionMessageServiceImplTest.class);

	@InjectMocks
	RejectionMessageServiceImpl rejectionMessageServiceImpl;

	@Mock
	private DapsTokenProviderService dapsProvider;
	@Mock
	private SelfDescriptionConfiguration selfDescriptionConfiguration;

	Message message;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
		message = UtilMessageService.getArtifactResponseMessage();
	}

	@ParameterizedTest
    @MethodSource("provideParameters")
    public void testRejectionMessagesWithMessage(RejectionMessageType messageType, String errorMessage) {
        
		logger.info("Testing rejection with reason {} and recevied message", messageType);
		
        ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
                () -> rejectionMessageServiceImpl.sendRejectionMessage(messageType, message));
        String message = exception.getMessage();
        assertTrue(message.contains(errorMessage));
        
    }
	
	@ParameterizedTest
    @MethodSource("provideParameters")
    public void testRejectionMessagesWithoutMessage(RejectionMessageType messageType, String errorMessage) {
        
		logger.info("Testing rejection with reason {} and without received message", messageType);
		
        ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
                () -> rejectionMessageServiceImpl.sendRejectionMessage(messageType, null));
        String message = exception.getMessage();
        assertTrue(message.contains(errorMessage));
        
    }

 

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES, RejectionReason.NOT_FOUND.getId().toString()),
                Arguments.of(RejectionMessageType.REJECTION_MESSAGE_COMMON, RejectionReason.MALFORMED_MESSAGE.getId().toString()),
                Arguments.of(RejectionMessageType.REJECTION_MESSAGE_LOCAL_ISSUES, RejectionReason.MALFORMED_MESSAGE.getId().toString()),
                Arguments.of(RejectionMessageType.REJECTION_TOKEN, RejectionReason.NOT_AUTHENTICATED.getId().toString()),
                Arguments.of(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, RejectionReason.NOT_AUTHENTICATED.getId().toString()),
                Arguments.of(RejectionMessageType.REJECTION_USAGE_CONTROL, RejectionReason.NOT_AUTHORIZED.getId().toString()),
                Arguments.of(RejectionMessageType.RESULT_MESSAGE, "")
        );
    }
}
