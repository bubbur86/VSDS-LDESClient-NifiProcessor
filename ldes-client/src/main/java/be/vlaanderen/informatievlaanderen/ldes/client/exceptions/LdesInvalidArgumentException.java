package be.vlaanderen.informatievlaanderen.ldes.client.exceptions;

public class LdesInvalidArgumentException extends LdesException {

	private static final long serialVersionUID = -8864222380853948805L;
	
	private final int exitCode;

	public LdesInvalidArgumentException(String message) {
		this(message, 0);
	}

	public LdesInvalidArgumentException(String message, int exitCode) {
		super("EXIT CODE " + exitCode + ": " + message);
		
		this.exitCode = exitCode;
	}
	
	public int getExitCode() {
		return exitCode;
	}
}
