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
		MockitoAnnotations.openMocks(this);
		when(dapsProvider.getDynamicAtributeToken()).thenReturn(UtilMessageService.getDynamicAttributeToken());
		when(selfDescriptionConfiguration.getConnectorURI()).thenReturn(URI.create("http://auto-generated"));
		message = UtilMessageService.getArtifactResponseMessage();
	}

	@ParameterizedTest
    @MethodSource("provideParameters")
    public void testRejectionMessagesWithMessage(RejectionReason rejectionReason, String errorMessage) {
        
		logger.info("Testing rejection with reason {} and recevied message", rejectionReason);
		
        ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
                () -> rejectionMessageServiceImpl.sendRejectionMessage(UtilMessageService.getArtifactRequestMessage(), rejectionReason));
        String message = exception.getMessage();
        assertTrue(message.contains(errorMessage));
        
    }
	
	@ParameterizedTest
    @MethodSource("provideParameters")
    public void testRejectionMessagesWithoutMessage(RejectionReason rejectionReason, String errorMessage) {
        
		logger.info("Testing rejection with reason {} and without received message", rejectionReason);
		
        ExceptionForProcessor exception = assertThrows(ExceptionForProcessor.class,
                () -> rejectionMessageServiceImpl.sendRejectionMessage(UtilMessageService.getArtifactRequestMessage(), rejectionReason));
        String message = exception.getMessage();
        assertTrue(message.contains(errorMessage));
        
    }

 

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(RejectionReason.NOT_FOUND, RejectionReason.NOT_FOUND.getId().toString()),
                Arguments.of(RejectionReason.MALFORMED_MESSAGE, RejectionReason.MALFORMED_MESSAGE.getId().toString()),
                Arguments.of(RejectionReason.BAD_PARAMETERS, RejectionReason.BAD_PARAMETERS.getId().toString()),
                Arguments.of(RejectionReason.NOT_AUTHENTICATED, RejectionReason.NOT_AUTHENTICATED.getId().toString()),
                Arguments.of(RejectionReason.INTERNAL_RECIPIENT_ERROR, RejectionReason.INTERNAL_RECIPIENT_ERROR.getId().toString()),
                Arguments.of(RejectionReason.NOT_AUTHORIZED, RejectionReason.NOT_AUTHORIZED.getId().toString()),
                Arguments.of(RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED, RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED.getId().toString()),
                Arguments.of(RejectionReason.METHOD_NOT_SUPPORTED, RejectionReason.METHOD_NOT_SUPPORTED.getId().toString()),
                Arguments.of(RejectionReason.TEMPORARILY_NOT_AVAILABLE, RejectionReason.TEMPORARILY_NOT_AVAILABLE.getId().toString()),
                Arguments.of(RejectionReason.TOO_MANY_RESULTS, RejectionReason.TOO_MANY_RESULTS.getId().toString()),
                Arguments.of(RejectionReason.VERSION_NOT_SUPPORTED, RejectionReason.VERSION_NOT_SUPPORTED.getId().toString())
        );
    }
}
