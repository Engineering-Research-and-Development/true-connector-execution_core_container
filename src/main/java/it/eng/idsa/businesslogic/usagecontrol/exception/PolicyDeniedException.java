package it.eng.idsa.businesslogic.usagecontrol.exception;

import java.io.IOException;
import java.util.Objects;

import org.springframework.lang.Nullable;

import retrofit2.Response;

public class PolicyDeniedException extends RuntimeException {
	private static final long serialVersionUID = 16974908248124628L;

	private static String getMessage(Response<?> response) {
		    Objects.requireNonNull(response, "response == null");
		    try {
				return "HTTP " + response.code() + " " + response.message() + " " + response.errorBody().string();
			} catch (IOException e) {
				return "HTTP " + response.code() + " " + response.message();
			}
		  }

		  private final int code;
		  private final String message;
		  private final transient Response<?> response;

		  public PolicyDeniedException(Response<?> response) {
		    super(getMessage(response));
		    this.code = response.code();
		    this.message = response.message();
		    this.response = response;
		  }

		  /**
		   * HTTP status code
		   * @return status code
		   */
		  public int code() {
		    return code;
		  }

		  /**
		   * HTTP status message
		   * @return Message
		   */
		  public String message() {
		    return message;
		  }

		  /**
		   * The full HTTP response. This may be null if the exception was serialized.
		   * @return Response
		   */
		  public @Nullable Response<?> response() {
		    return response;
		  }
		}
