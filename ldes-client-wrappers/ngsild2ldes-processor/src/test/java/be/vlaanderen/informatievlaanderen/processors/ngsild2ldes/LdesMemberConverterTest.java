package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services.LdesMemberConverter;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LdesMemberConverterTest {

    private static final String DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH = "$.dateObserved.value['@value']";
    private static final String DEFAULT_ID_JSON_PATH = "$.id";
    private static final String DEFAULT_DELIMITER = "/";
    private static final String DEFAULT_VERSION_OF_KEY = "http://purl.org/dc/terms/isVersionOf";

    LdesMemberConverter ldesMemberConverter = new LdesMemberConverter(DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH, DEFAULT_ID_JSON_PATH, DEFAULT_DELIMITER, DEFAULT_VERSION_OF_KEY);

    private static String jsonString;


    @BeforeEach
    void init() throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-ngsild.json")).toURI());
        jsonString = Files.readString(file.toPath());
    }

    @Test
    void shouldMatchCountOfObjects() {
        String id = ldesMemberConverter.convert(jsonString);
        assertEquals("urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR/2022-04-19T11:40:42.000Z", JsonPath.read(id, DEFAULT_ID_JSON_PATH));
    }

}