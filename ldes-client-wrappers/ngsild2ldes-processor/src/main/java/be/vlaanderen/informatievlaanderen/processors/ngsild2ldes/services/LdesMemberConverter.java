package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class LdesMemberConverter {

    private final String dateObservedValueJsonPath;
    private final String idJsonPath;
    private final String delimiter;
    private final String versionOfKey;

    public LdesMemberConverter(String dateObservedValueJsonPath, String idJsonPath, String delimiter, String versionOfKey) {
        this.dateObservedValueJsonPath = dateObservedValueJsonPath;
        this.idJsonPath = idJsonPath;
        this.delimiter = delimiter;
        this.versionOfKey = versionOfKey;
    }

    public String convert(final String jsonString) {
        String versionObjectId = generateId(jsonString);
        String baseId = JsonPath.read(jsonString, idJsonPath);
        DocumentContext documentContext = JsonPath.using(configuration).parse(jsonString).set("$.id", versionObjectId);
        final ObjectNode root = getVersionOfNode(baseId);
        JsonNode versionOf = addVersionOfNode(documentContext, root);
        return versionOf.toString();
    }

    private JsonNode addVersionOfNode(DocumentContext documentContext, ObjectNode root) {
        ObjectNode json = documentContext.json();
        return json.set(versionOfKey, root);
    }

    private ObjectNode getVersionOfNode(String baseId) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        root.set("type", mapper.convertValue("Relationship", JsonNode.class));
        root.set("object", mapper.convertValue(baseId, JsonNode.class));
        return root;
    }

    public String generateId(String jsonString) {
        String dateObserved = JsonPath.read(jsonString, dateObservedValueJsonPath);
        String id = JsonPath.read(jsonString, idJsonPath);
        return id + delimiter + dateObserved;
    }

    private static final Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();


}
