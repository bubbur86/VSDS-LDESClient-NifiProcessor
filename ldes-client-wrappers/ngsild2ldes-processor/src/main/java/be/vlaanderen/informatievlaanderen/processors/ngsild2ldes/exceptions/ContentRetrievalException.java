package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.exceptions;

public class ContentRetrievalException extends RuntimeException {

    private final long id;

    public ContentRetrievalException(long id, Throwable cause) {
        super(cause);
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "Content of Flowfile " + id + " cannot be retrieved";
    }
}
