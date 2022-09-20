package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config;

import org.apache.nifi.processor.Relationship;

public class NgsiLd2LdesMemberProcessorRelationships {

    private NgsiLd2LdesMemberProcessorRelationships() {}

    public static final Relationship DATA_RELATIONSHIP = new Relationship.Builder().name("data")
            .description("Posts LDES members to the remote URL").build();
}
