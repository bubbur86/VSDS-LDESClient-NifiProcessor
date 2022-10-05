package be.vlaanderen.informatievlaanderen.ldes.processors;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.ADD_TOP_LEVEL_GENERATED_AT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.ADD_WKT_PROPERTY;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.DATE_OBSERVED_VALUE_JSON_PATH;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.DELIMITER;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.ID_JSON_PATH;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.USE_SIMPLE_VERSION_OF;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors.VERSION_OF_KEY;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorRelationships.DATA_RELATIONSHIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiLdToLdesMemberProcessorPropertyDescriptors;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.FlowManager;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.LdesMemberConverter;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.MemberInfoExtractor;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.OutputFormatConverter;
import be.vlaanderen.informatievlaanderen.ldes.processors.services.WKTUpdater;
import be.vlaanderen.informatievlaanderen.ldes.processors.valueobjects.MemberInfo;


@Tags({"ngsild, ldes, vsds"})
@CapabilityDescription("Converts NGSI-LD to LdesMembers and send them to the next processor")
public class NgsiLdToLdesMemberProcessor extends AbstractProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NgsiLdToLdesMemberProcessor.class);

    private final WKTUpdater wktUpdater = new WKTUpdater();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;
    private LdesMemberConverter ldesMemberConverter;
    private OutputFormatConverter outputFormatConverter;
    private MemberInfoExtractor memberInfoExtractor;
    private boolean addWKTProperty;


    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(DATE_OBSERVED_VALUE_JSON_PATH);
        descriptors.add(ID_JSON_PATH);
        descriptors.add(DELIMITER);
        descriptors.add(VERSION_OF_KEY);
        descriptors.add(DATA_DESTINATION_FORMAT);
        descriptors.add(ADD_TOP_LEVEL_GENERATED_AT);
        descriptors.add(USE_SIMPLE_VERSION_OF);
        descriptors.add(ADD_WKT_PROPERTY);
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
        String dateObservedValueJsonPath = NgsiLdToLdesMemberProcessorPropertyDescriptors.getDateObservedValueJsonPath(context);
        String idJsonPath = NgsiLdToLdesMemberProcessorPropertyDescriptors.getIdJsonPath(context);
        String delimiter = NgsiLdToLdesMemberProcessorPropertyDescriptors.getDelimiter(context);
        String versionOfKey = NgsiLdToLdesMemberProcessorPropertyDescriptors.getVersionOfKey(context);
        Lang dataDestionationFormat = NgsiLdToLdesMemberProcessorPropertyDescriptors.getDataDestinationFormat(context);
        boolean addTopLevelGeneratedAt = NgsiLdToLdesMemberProcessorPropertyDescriptors.isAddTopLevelGeneratedAt(context);
        boolean useSimpleVersionOf = NgsiLdToLdesMemberProcessorPropertyDescriptors.isUseSimpleVersionOf(context);
        addWKTProperty = NgsiLdToLdesMemberProcessorPropertyDescriptors.isAddWKTProperty(context);

        memberInfoExtractor = new MemberInfoExtractor(dateObservedValueJsonPath, idJsonPath);
        ldesMemberConverter = new LdesMemberConverter(dateObservedValueJsonPath, idJsonPath, delimiter, versionOfKey, useSimpleVersionOf);
        outputFormatConverter = new OutputFormatConverter(dataDestionationFormat, addTopLevelGeneratedAt);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        LOGGER.info("On Trigger");
        FlowFile flowFile = session.get();

        String content = FlowManager.receiveData(session, flowFile);
        if (addWKTProperty)
            content = wktUpdater.updateGeoPropertyStatements(content);
        MemberInfo memberInfo = memberInfoExtractor.extractMemberInfo(content);
        String convert = ldesMemberConverter.convert(content);
        String s = outputFormatConverter.convertToDesiredOutputFormat(convert, memberInfo);
        
        FlowManager.sendRDFToRelation(session, outputFormatConverter.getOutputFormat(), s, DATA_RELATIONSHIP, flowFile);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(addWKTProperty, descriptors, ldesMemberConverter,
				memberInfoExtractor, outputFormatConverter, relationships, wktUpdater);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NgsiLdToLdesMemberProcessor other = (NgsiLdToLdesMemberProcessor) obj;
		return addWKTProperty == other.addWKTProperty
				&& Objects.equals(descriptors, other.descriptors)
				&& Objects.equals(ldesMemberConverter, other.ldesMemberConverter)
				&& Objects.equals(memberInfoExtractor, other.memberInfoExtractor)
				&& Objects.equals(outputFormatConverter, other.outputFormatConverter)
				&& Objects.equals(relationships, other.relationships) && Objects.equals(wktUpdater, other.wktUpdater);
	}
}