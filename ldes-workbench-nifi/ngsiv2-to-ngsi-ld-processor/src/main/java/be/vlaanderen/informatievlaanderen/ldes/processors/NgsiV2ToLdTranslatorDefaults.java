package be.vlaanderen.informatievlaanderen.ldes.processors;

import static be.vlaanderen.informatievlaanderen.ldes.processors.LdesWorkbenchDefaults.NIFI_FALSE;

public class NgsiV2ToLdTranslatorDefaults {
	
	private NgsiV2ToLdTranslatorDefaults() {}
	
	public static final String DEFAULT_CORE_CONTEXT = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";
	public static final String LD_CONTEXT_WATER_QUALITY_OBSERVED = null;
	public static final String DEFAULT_ADD_WKT_FOR_GEOJSON_PROPERTIES = NIFI_FALSE;
	
	private static final String[] NGSI_LD_ALLOWED_URI_SCHEMES = new String[] { "urn", "http", "https" };
	
	public static final String NGSI_LD_URI_PREFIX = "urn:ngsi-ld";
	public static final String NGSI_LD_REFERENCE_PREFIX = "ref";
	
	public static String[] getAllowedSchemes() {
		return NGSI_LD_ALLOWED_URI_SCHEMES;
	}
}
