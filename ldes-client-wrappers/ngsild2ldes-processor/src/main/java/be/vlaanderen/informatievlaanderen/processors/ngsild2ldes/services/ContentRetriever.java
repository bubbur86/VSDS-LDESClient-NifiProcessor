package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.exceptions.ContentRetrievalException;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ContentRetriever {

    public String getContent(ByteArrayOutputStream bytes, ProcessSession session, FlowFile flowFile) {
        try {
            session.exportTo(flowFile, bytes);
            bytes.close();
            return bytes.toString();
        } catch (IOException e) {
            throw new ContentRetrievalException(flowFile.getId(),e);
        }
    }
}
