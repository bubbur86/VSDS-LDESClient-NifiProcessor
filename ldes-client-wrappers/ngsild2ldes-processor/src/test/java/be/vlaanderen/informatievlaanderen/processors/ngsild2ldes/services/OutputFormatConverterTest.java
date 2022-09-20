package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputFormatConverterTest {

    @Test
    void when_RequestedFormatIsJsonLD11_OutputIsEqualToInput() throws URISyntaxException, IOException {
        String jsonString = getJsonString();
        OutputFormatConverter outputFormatConverter = new OutputFormatConverter(Lang.JSONLD11);

        String actualOutput = outputFormatConverter.convertToDesiredOutputFormat(jsonString);

        assertEquals(jsonString, actualOutput);
    }

    @Test
    void when_RequestedFormatIsNQuads_InputIsConvertedToNQuads() throws URISyntaxException, IOException {
        String jsonString = getJsonString();
        OutputFormatConverter outputFormatConverter = new OutputFormatConverter(Lang.NQUADS);

        String actualOutput = outputFormatConverter.convertToDesiredOutputFormat(jsonString);

        assertTrue(actualOutput.contains("<urn:ngsi-v2:cot-imec-be:WaterQualityObserved:imec-iow-3orY3reQDK5n3TMpPnLVYR/2022-04-19T11:40:42.000Z> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://uri.etsi.org/ngsi-ld/default-context/WaterQualityObserved> ."));
    }
    private String getJsonString() throws IOException, URISyntaxException {
        Optional<String> optionalJsonString = Files
                .lines(
                        Paths.get(
                                Objects.requireNonNull(
                                                getClass().getClassLoader().getResource("outputformat/example-ldes.json"))
                                        .toURI()
                        )
                )
                .findFirst();

        return optionalJsonString.orElseThrow(RuntimeException::new);
    }

}