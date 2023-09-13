package it.eng.idsa.businesslogic.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CorrelationIdFilterTest {

	@InjectMocks
	private CorrelationIdFilter filter;
	
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void doFilter() throws ServletException, IOException {
		when(request.getRequestURI()).thenReturn("/someuri/api/test");
		
		filter.doFilter(request, response, filterChain);
		
		verify(filterChain).doFilter(request, response);
	}
}
