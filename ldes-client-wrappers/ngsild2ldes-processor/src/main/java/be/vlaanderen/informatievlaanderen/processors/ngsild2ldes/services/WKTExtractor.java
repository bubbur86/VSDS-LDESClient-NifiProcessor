package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class WKTExtractor {
    private static final String LONGITUDE_JSON_PATH = "$.location.value.coordinates[0]";
    private static final String LATITUDE_JSON_PATH = "$.location.value.coordinates[1]";

    public String extractWKT(String jsonString) {
        try {
            double longitude = JsonPath.read(jsonString, LONGITUDE_JSON_PATH);
            double latitude = JsonPath.read(jsonString, LATITUDE_JSON_PATH);
            return "POINT(" + longitude + " " + latitude + ")";
        } catch (PathNotFoundException pathNotFoundException) {
            return null;
        }
    }
}
