package be.vlaanderen.informatievlaanderen.ldes.processors.config;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.DEFAULT_ADD_WKT_FOR_GEOJSON_PROPERTIES;
import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.DEFAULT_CORE_CONTEXT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.NIFI_FALSE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults.NIFI_TRUE;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

import be.vlaanderen.informatievlaanderen.ldes.processors.validators.RDFLanguageValidator;

public class NgsiV2ToLdProcessorProperties {

	private NgsiV2ToLdProcessorProperties() {}

    public static final PropertyDescriptor CORE_CONTEXT =
            new PropertyDescriptor
                    .Builder()
                    .name("CORE_CONTEXT")
                    .displayName("NGSI LD core context")
                    .description("URL of the NGSI LD context")
                    .required(false)
                    .addValidator(StandardValidators.URL_VALIDATOR)
                    .defaultValue(DEFAULT_CORE_CONTEXT)
                    .build();

    public static final PropertyDescriptor LD_CONTEXT =
            new PropertyDescriptor
                    .Builder()
                    .name("LD_CONTEXT")
                    .displayName("NGSI LD context")
                    .description("URL of the NGSI LD context for this dataset")
                    .required(true)
                    .addValidator(StandardValidators.URL_VALIDATOR)
                    .build();

    public static final PropertyDescriptor DATA_SOURCE_FORMAT =
            new PropertyDescriptor
                    .Builder()
                    .name("DATA_SOURCE_FORMAT")
                    .displayName("Data source format")
                    .description("RDF format identifier of the data source")
                    .required(false)
                    .addValidator(new RDFLanguageValidator())
                    .defaultValue(DEFAULT_DATA_SOURCE_FORMAT)
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

    public static final PropertyDescriptor ADD_WKT_FOR_GEOJSON_PROPERTIES =
            new PropertyDescriptor
                    .Builder()
                    .name("ADD_WKT_FOR_GEOJSON_PROPERTIES")
                    .displayName("Translate geoJSON properties to wkt literals")
                    .description("Translate geoJSON properties to wkt literals")
                    .required(false)
                    .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                    .defaultValue(DEFAULT_ADD_WKT_FOR_GEOJSON_PROPERTIES)
                    .allowableValues(NIFI_TRUE, NIFI_FALSE)
                    .build();

    public static String getCoreContext(final ProcessContext context) {
    	return context.getProperty(CORE_CONTEXT).getValue();
    }

    public static String getLdContext(final ProcessContext context) {
    	return context.getProperty(LD_CONTEXT).getValue();
    }
    
    public static Lang getDataSourceFormat(final ProcessContext context) {
    	return RDFLanguages.nameToLang(context.getProperty(DATA_SOURCE_FORMAT).getValue());
    }
    
    public static Lang getDataDestinationFormat(final ProcessContext context) {
    	return RDFLanguages.nameToLang(context.getProperty(DATA_DESTINATION_FORMAT).getValue());
    }
    
    public static boolean getAddWktForGeoJSONProperties(final ProcessContext context) {
    	return context.getProperty(ADD_WKT_FOR_GEOJSON_PROPERTIES).asBoolean();
    }
}
