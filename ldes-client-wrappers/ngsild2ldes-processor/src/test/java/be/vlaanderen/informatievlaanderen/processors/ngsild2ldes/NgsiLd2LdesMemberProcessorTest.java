package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config.NgsiLd2LdesMemberProcessorRelationships.DATA_RELATIONSHIP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class NgsiLd2LdesMemberProcessorTest {

    private static final String DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH = "$.dateObserved.value['@value']";
    private static final String DEFAULT_ID_JSON_PATH = "$.id";
    private static final String DEFAULT_DELIMITER = "/";
    private static final String DEFAULT_VERSION_OF_KEY = "http://purl.org/dc/terms/isVersionOf";
    private static final String DEFAULT_DATA_DESTINATION_FORMAT = "n-quads";

    private TestRunner testRunner;

    @BeforeEach
    public void init() {
        testRunner = TestRunners.newTestRunner(NgsiLd2LdesMemberProcessor.class);
    }

    @Test
    void when_runningLdesClientWithConnectedFragments_expectsAllLdesMembers() throws IOException, URISyntaxException {
        testRunner.setProperty("DATE_OBSERVED_VALUE_JSON_PATH", DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH);
        testRunner.setProperty("ID_JSON_PATH", DEFAULT_ID_JSON_PATH);
        testRunner.setProperty("DELIMITER", DEFAULT_DELIMITER);
        testRunner.setProperty("VERSION_OF_KEY", DEFAULT_VERSION_OF_KEY);
        testRunner.setProperty("DATA_DESTINATION_FORMAT", DEFAULT_DATA_DESTINATION_FORMAT);
        final Path JSON_SNIPPET = Paths.get(String.valueOf(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-ngsild.json")).toURI())));
        testRunner.enqueue(JSON_SNIPPET);
        testRunner.run();

        List<MockFlowFile> dataFlowfiles = testRunner.getFlowFilesForRelationship(DATA_RELATIONSHIP);
        assertEquals(1, dataFlowfiles.size());
        String content = dataFlowfiles.get(0).getContent();
        Model model = readLdesMemberFromFile(getClass().getClassLoader(), "expected-n-quads-result.nq");
        assertTrue(model.isIsomorphicWith(getModel(content, Lang.NQUADS)));
    }

    private Model readLdesMemberFromFile(ClassLoader classLoader, String fileName)
            throws URISyntaxException, IOException {
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).toURI());

        return getModel(Files.lines(Paths.get(file.toURI())).collect(Collectors.joining()), Lang.NQUADS);
    }

    private Model getModel(String s, Lang lang) {
        return RDFParserBuilder.create()
                .fromString(s).lang(lang)
                .toModel();
    }


}
