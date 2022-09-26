package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class WKTExtractorTest {

    WKTExtractor wktExtractor = new WKTExtractor();

    @Test
    void when_GeometryPresent_WKTIsPoint() throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-device.json")).toURI());
        String jsonString = Files.readString(file.toPath());
        String wkt = wktExtractor.extractWKT(jsonString);
        assertEquals("POINT(5.4563 51.41363)", wkt);
    }

    @Test
    void when_noGeometryPresent_WKTisNull() throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-device-model.json")).toURI());
        String jsonString = Files.readString(file.toPath());
        String wkt = wktExtractor.extractWKT(jsonString);
        assertNull( wkt);
    }

}