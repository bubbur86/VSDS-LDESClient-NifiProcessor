package be.vlaanderen.informatievlaanderen.ldes.processors.exceptions;

public class InvalidNgsiLdURIException extends RuntimeException {

	/** Implements Serializable. */
	private static final long serialVersionUID = -3966400151898120863L;

	public InvalidNgsiLdURIException(String message) {
		super(message);
	}
	
	public InvalidNgsiLdURIException(String message, Throwable cause) {
		super(message, cause);
	}
}
