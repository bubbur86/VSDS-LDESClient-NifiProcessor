package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.config;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.validators.RDFLanguageValidator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

public final class NgsiLd2LdesMemberProcessorPropertyDescriptors {

    private static final String DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH = "$.dateObserved.value['@value']";
    private static final String DEFAULT_ID_JSON_PATH = "$.id";
    private static final String DEFAULT_DELIMITER = "/";
    private static final String DEFAULT_VERSION_OF_KEY = "http://purl.org/dc/terms/isVersionOf";
    private static final String DEFAULT_DATA_DESTINATION_FORMAT = "n-quads";


    private NgsiLd2LdesMemberProcessorPropertyDescriptors() {}

    public static final PropertyDescriptor DATE_OBSERVED_VALUE_JSON_PATH =
            new PropertyDescriptor
                    .Builder()
                    .name("DATE_OBSERVED_VALUE_JSON_PATH")
                    .displayName("JsonPath to the value of the dateObserved element")
                    .description("JsonPath to the value of the dateObserved element, e.g. "+DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH)
                    .required(false)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .defaultValue(DEFAULT_DATE_OBSERVED_VALUE_JSON_PATH)
                    .build();

    public static final PropertyDescriptor ID_JSON_PATH =
            new PropertyDescriptor
                    .Builder()
                    .name("ID_JSON_PATH")
                    .displayName("JsonPath to the id")
                    .description("JsonPath to id, e.g. "+DEFAULT_ID_JSON_PATH)
                    .required(false)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .defaultValue(DEFAULT_ID_JSON_PATH)
                    .build();

    public static final PropertyDescriptor DELIMITER =
            new PropertyDescriptor
                    .Builder()
                    .name("DELIMITER")
                    .displayName("Delimiter used to create the version object id")
                    .description("Delimiter used to create the version object id")
                    .required(false)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .defaultValue(DEFAULT_DELIMITER)
                    .build();

    public static final PropertyDescriptor VERSION_OF_KEY =
            new PropertyDescriptor
                    .Builder()
                    .name("VERSION_OF_KEY")
                    .displayName("VersionOf Property")
                    .description("VersionOf Property, e.g. "+DEFAULT_VERSION_OF_KEY)
                    .required(false)
                    .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                    .defaultValue(DEFAULT_VERSION_OF_KEY)
                    .build();

    public static final PropertyDescriptor DATA_DESTINATION_FORMAT =
            new PropertyDescriptor
                    .Builder()
                    .name("DATA_DESTINATION_FORMAT")
                    .displayName("Data destination format")
                    .description("RDF format identifier of the data destination")
                    .required(false)
                    .addValidator(new RDFLanguageValidator())
                    .defaultValue(DEFAULT_DATA_DESTINATION_FORMAT)
                    .build();

    public static String getDateObservedValueJsonPath(ProcessContext context) {
        return context.getProperty(DATE_OBSERVED_VALUE_JSON_PATH).getValue();
    }

    public static String getIdJsonPath(ProcessContext context) {
        return context.getProperty(ID_JSON_PATH).getValue();
    }

    public static String getDelimiter(ProcessContext context) {
        return context.getProperty(DELIMITER).getValue();
    }

    public static String getVersionOfKey(ProcessContext context) {
        return context.getProperty(VERSION_OF_KEY).getValue();
    }

    public static Lang getDataDestinationFormat(ProcessContext context) {
        return RDFLanguages.nameToLang(context.getProperty(DATA_DESTINATION_FORMAT).getValue());
    }
}