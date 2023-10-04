/**
 * 
 */
package it.eng.idsa.businesslogic.util.exception;

/**
 * @author gianluca
 *
 */
public class OCSPValidationException extends Exception {

	private static final long serialVersionUID = -4467886078464845582L;

	public OCSPValidationException() {
		super();
	}
	
	public OCSPValidationException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public OCSPValidationException(String message) {
		super(message);
	}
	
	
	
}
