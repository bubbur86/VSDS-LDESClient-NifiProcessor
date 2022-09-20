package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.exceptions.ContentRetrievalException;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.MockProcessSession;
import org.apache.nifi.util.SharedSessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class ContentRetrieverTest {

    ContentRetriever contentRetriever = new ContentRetriever();

    private static final String CONTENT = "data of mock";
    private MockProcessSession session;
    private MockFlowFile mockFlowFile;

    @BeforeEach
    void setUp() {
        final Processor processor = Mockito.mock(Processor.class);
        final AtomicLong idGenerator = new AtomicLong(0L);
        session = new MockProcessSession(new SharedSessionState(processor, idGenerator), processor);
        mockFlowFile = session.create();
    }

    @Test
    void when_ContentCanBeRead_ContentIsReturned() {
        mockFlowFile.setData(CONTENT.getBytes(StandardCharsets.UTF_8));

        String actualContent = contentRetriever.getContent(new ByteArrayOutputStream(), session, mockFlowFile);

        assertEquals(CONTENT, actualContent);
    }

    @Test
    void when_RetrievingOfContentThrowsIOException_ContentRetrievalExceptionIsThrown() throws IOException {
        mockFlowFile.setData(CONTENT.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream byteArrayOutputStream = mock(ByteArrayOutputStream.class);
        doThrow(new IOException()).when(byteArrayOutputStream).close();

        ContentRetrievalException contentRetrievalException = assertThrows(ContentRetrievalException.class, () -> contentRetriever.getContent(byteArrayOutputStream, session, mockFlowFile));

        assertEquals("Content of Flowfile 0 cannot be retrieved", contentRetrievalException.getMessage());
    }

}