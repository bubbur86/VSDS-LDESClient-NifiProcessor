package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class WKTUpdaterTest {

    WKTUpdater wktUpdater = new WKTUpdater();

    @Test
    void when_GeometryPresent_WKTIsPoint() throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-waterqualityobserved.json")).toURI());
        String jsonString = Files.readString(file.toPath());
        String wkt = wktUpdater.updateGeoPropertyStatements(jsonString);
        assertTrue(wkt.contains("asWKT"));
    }

    @Test
    void when_noGeometryPresent_WKTisNull() throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-device-model.json")).toURI());
        String jsonString = Files.readString(file.toPath());
        String wkt = wktUpdater.updateGeoPropertyStatements(jsonString);
        assertFalse(wkt.contains("asWKT"));
    }

}