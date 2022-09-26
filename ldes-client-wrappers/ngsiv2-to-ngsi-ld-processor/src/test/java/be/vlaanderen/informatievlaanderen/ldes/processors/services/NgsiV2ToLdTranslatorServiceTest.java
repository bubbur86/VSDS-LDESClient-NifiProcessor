package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_CREATED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_MODIFIED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_ID;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.translateKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.processors.NgsiV2ToLdTranslatorDefaults;

@WireMockTest(httpPort=10101)
class NgsiV2ToLdTranslatorServiceTest {

	private String localCoreContext = "http://localhost:10101/ngsi-ld-core-context.json";
	private String localLdContext = "http://localhost:10101/water-quality-observed-context.json";
	private String remoteCoreContext = NgsiV2ToLdTranslatorDefaults.DEFAULT_CORE_CONTEXT;
	private String remoteLdContext = NgsiV2ToLdTranslatorDefaults.LD_CONTEXT_WATER_QUALITY_OBSERVED;

	private String idV2 = "waterqualityobserved:Sevilla:D1";
	private String type = "WaterQualityObserved";

	private NgsiV2ToLdTranslatorService translator;
	private JsonObject data = new JsonObject(); 

	@BeforeEach
	void setup() {
		data.put(NGSI_V2_KEY_ID, idV2);
		data.put(NGSI_V2_KEY_TYPE, type);
	}

	@Test
	void whenIdFound_thenIdTranslated() {
		translator = new NgsiV2ToLdTranslatorService(localCoreContext, localLdContext);
		
		String idLd = "urn:ngsi-ld:WaterQualityObserved:" + idV2;

		JsonObject model = translator.translate(data.toString()).getModel();

		assertEquals(idLd, model.get(translateKey(NGSI_V2_KEY_ID)).getAsString().value(), "Translate ID");
	}

	@Test
	void whenTypeFound_thenTypeTranslated() {
		translator = new NgsiV2ToLdTranslatorService(localCoreContext, localLdContext);
		
		JsonObject model = translator.translate(data.toString()).getModel();

		assertEquals(type, model.get(translateKey(NGSI_V2_KEY_TYPE)).getAsString().value(), "Translate type");
	}
	
	@Test
	void whenDateFound_thenDateNormalised() {
		translator = new NgsiV2ToLdTranslatorService(localCoreContext, localLdContext);
		
		String expectedDate = "2017-01-31T06:45:00Z";
		JsonObject model;
		
		data.put(NGSI_V2_KEY_DATE_CREATED, "2017-01-31T06:45:00");
		model = translator.translate(data.toString()).getModel();

		assertEquals(expectedDate, model.get(translateKey(NGSI_V2_KEY_DATE_CREATED)).getAsString().value(), "Translate a date");
		

		data.put(NGSI_V2_KEY_DATE_CREATED, expectedDate);
		model = translator.translate(data.toString()).getModel();

		assertEquals(expectedDate, model.get(translateKey(NGSI_V2_KEY_DATE_CREATED)).getAsString().value(), "Translate a date");
	}

	@Test
	void whenDateCreatedFound_thenDateCreatedTranslated() {
		translator = new NgsiV2ToLdTranslatorService(localCoreContext, localLdContext);
		
		data.put(NGSI_V2_KEY_DATE_CREATED, "2017-01-31T06:45:00");
		JsonObject model = translator.translate(data.toString()).getModel();

		assertEquals("2017-01-31T06:45:00Z", model.get(translateKey(NGSI_V2_KEY_DATE_CREATED)).getAsString().value(), "Translate dateCreated");
	}

	@Test
	void whenDateModifiedFound_thenDateModifiedTranslated() {
		translator = new NgsiV2ToLdTranslatorService(localCoreContext, localLdContext);
		
		data.put(NGSI_V2_KEY_DATE_MODIFIED, "2017-01-31T06:45:00");
		JsonObject model = translator.translate(data.toString()).getModel();

		assertEquals("2017-01-31T06:45:00Z", model.get(translateKey(NGSI_V2_KEY_DATE_MODIFIED)).getAsString().value(), "Translate dateModified");
	}
	
	@Test
	void whenDeviceNgsiIsInput_thenDeviceNgsiIsTranslatedWithLocalContext() throws Exception {
		testTranslationLocalContext("device_ngsiv2.json", "device_ngsild.json", "Translate Device NGSIv2 (local context)");
	}
	
	@Test
	void whenDeviceNgsiIsInput_thenDeviceNgsiIsTranslatedWithRemoteContext() throws Exception {
		testTranslationRemoteContext("device_ngsiv2.json", "device_ngsild.json", "Translate Device NGSIv2 (remote context)");
	}
	
	@Test
	void whenDeviceNgsiIsInputWithWKTTranslationTrue_thenDeviceNgsiIsTranslatedWithLocalContextAndWktTranslation() throws Exception {
		testTranslationLocalContext("device_ngsiv2.json", "device_ngsild.json", "Translate Device NGSIv2 (local context), with geo:json -> wkt translation added");
	}
	
	@Test
	void whenDeviceModelNgsiIsInput_thenDeviceModelNgsiIsTranslatedLocalContext() throws Exception {
		testTranslationLocalContext("device_model_ngsiv2.json", "device_model_ngsild.json", "Translate DeviceModel NGSIv2 (local context)");
	}
	
	@Test
	void whenDeviceModelNgsiIsInput_thenDeviceModelNgsiIsTranslatedWithRemoteContext() throws Exception {
		testTranslationRemoteContext("device_model_ngsiv2.json", "device_model_ngsild.json", "Translate DeviceModel NGSIv2 (remote context)");
	}
	
	@Test
	void whenDeviceModelNgsiIsInputWithWKTTranslationTrue_thenDeviceModelNgsiIsTranslatedWithLocalContextAndWktTranslation() throws Exception {
		testTranslationLocalContext("device_model_ngsiv2.json", "device_model_ngsild.json", "Translate DeviceModel NGSIv2 (local context), with geo:json -> wkt translation added");
	}
	
	@Test
	void whenWaterQualityObservedNgsiIsInput_thenWaterQualityObservedNgsiIsTranslatedWithLocalContext() throws Exception {
		testTranslationLocalContext("water_quality_observed_ngsiv2.json", "water_quality_observed_ngsild.json", "Translate WaterQualityObserved NGSIv2 (local context)");
	}
	
	@Test
	void whenWaterQualityObservedNgsiIsInput_thenWaterQualityObservedNgsiIsTranslatedWithRemoteContext() throws Exception {
		testTranslationRemoteContext("water_quality_observed_ngsiv2.json", "water_quality_observed_ngsild.json", "Translate WaterQualityObserved NGSIv2 (remote context)");
	}
	
	@Test
	void whenWaterQualityObservedNgsiIsInputWithWKTTranslationTrue_thenWaterQualityObservedNgsiIsTranslatedWithLocalContextAndWktTranslation() throws Exception {
		testTranslationLocalContext("water_quality_observed_ngsiv2.json", "water_quality_observed_ngsild.json", "Translate WaterQualityObserved NGSIv2 (local context), with geo:json -> wkt translation added");
	}
	
	private void testTranslationLocalContext(String input, String expected, String message) throws Exception {
		testTranslation(true, input, expected, message);
	}
	
	private void testTranslationRemoteContext(String input, String expected, String message) throws Exception {
		testTranslation(false, input, expected, message);
	}
	
	private void testTranslation(boolean local, String input, String expected, String message) throws Exception {
		translator = new NgsiV2ToLdTranslatorService(local ? localCoreContext : remoteCoreContext, local ? localLdContext: remoteLdContext);
		
		Path v2 = Path.of("src/test/resources/" + input);
		Path ld = Path.of("src/test/resources/" + (local ? "local" : "remote") + "_context/" + expected);
		
		Model v2Model = translator.translate(Files.readString(v2)).toRDFModel(Lang.JSONLD11);
		Model ldModel = ModelFactory.createDefaultModel();
		RDFParser.source(ld)
				.forceLang(Lang.JSONLD11)
				.parse(ldModel);
		
		assertTrue(ldModel.isIsomorphicWith(v2Model), message);
	}
}
