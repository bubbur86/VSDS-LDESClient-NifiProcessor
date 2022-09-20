package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config.NgsiLd2LdesMemberProcessorPropertyDescriptors;
import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services.ContentRetriever;
import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services.LdesMemberConverter;
import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services.OutputFormatConverter;
import org.apache.jena.riot.Lang;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config.NgsiLd2LdesMemberProcessorPropertyDescriptors.*;
import static be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config.NgsiLd2LdesMemberProcessorRelationships.DATA_RELATIONSHIP;


@Tags({"ngsild, ldes, vsds"})
@CapabilityDescription("Converts NGSI-LD to LdesMembers and send them to the next processor")
public class NgsiLd2LdesMemberProcessor extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NgsiLd2LdesMemberProcessor.class);

    private final ContentRetriever contentRetriever = new ContentRetriever();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;
    private LdesMemberConverter ldesMemberConverter;
    private OutputFormatConverter outputFormatConverter;


    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(DATE_OBSERVED_VALUE_JSON_PATH);
        descriptors.add(ID_JSON_PATH);
        descriptors.add(DELIMITER);
        descriptors.add(VERSION_OF_KEY);
        descriptors.add(DATA_DESTINATION_FORMAT);
        descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(DATA_RELATIONSHIP);
        relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        LOGGER.info("On Schedule");
        String dateObservedValueJsonPath = NgsiLd2LdesMemberProcessorPropertyDescriptors.getDateObservedValueJsonPath(context);
        String idJsonPath = NgsiLd2LdesMemberProcessorPropertyDescriptors.getIdJsonPath(context);
        String delimiter = NgsiLd2LdesMemberProcessorPropertyDescriptors.getDelimiter(context);
        String versionOfKey = NgsiLd2LdesMemberProcessorPropertyDescriptors.getVersionOfKey(context);
        Lang dataDestionationFormat = NgsiLd2LdesMemberProcessorPropertyDescriptors.getDataDestinationFormat(context);

        ldesMemberConverter = new LdesMemberConverter(dateObservedValueJsonPath, idJsonPath, delimiter, versionOfKey);
        outputFormatConverter = new OutputFormatConverter(dataDestionationFormat);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        LOGGER.info("On Trigger");
        FlowFile flowFile = session.get();

        String content = contentRetriever.getContent(new ByteArrayOutputStream(), session, flowFile);

        String convert = ldesMemberConverter.convert(content);
        String s = outputFormatConverter.convertToDesiredOutputFormat(convert);

        FlowFile nextFlowFile = session.write(flowFile, outputStream -> {
            outputStream.write(s.getBytes());
            outputStream.flush();
        });
        session.transfer(nextFlowFile, DATA_RELATIONSHIP);
    }


}
