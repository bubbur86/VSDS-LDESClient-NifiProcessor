package be.vlaanderen.informatievlaanderen.ldes.client.exception;

public class UnparseableFragmentException extends RuntimeException{
    private final String fragmentId;

    public UnparseableFragmentException(String fragmentId, Throwable cause) {
        super(cause);
        this.fragmentId = fragmentId;
    }

    @Override
    public String getMessage() {
        return "LdesClient cannot parse fragment id: "+fragmentId;
    }
}
