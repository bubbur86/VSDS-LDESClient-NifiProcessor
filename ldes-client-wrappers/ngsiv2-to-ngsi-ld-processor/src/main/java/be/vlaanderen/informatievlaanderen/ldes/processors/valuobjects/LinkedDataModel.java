package be.vlaanderen.informatievlaanderen.ldes.processors.valuobjects;

import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_LD_CONTEXT;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_MODIFIED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_OBSERVED;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_ID;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.NGSI_V2_KEY_TYPE;
import static be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping.translateKey;

import java.util.List;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

import be.vlaanderen.informatievlaanderen.ldes.processors.config.NgsiV2ToLdMapping;

public class LinkedDataModel {

    private final JsonObject jsonModel = new JsonObject();
    private Lang dataSourceFormat;

	public LinkedDataModel(Lang dataSourceFormat) {
		this.dataSourceFormat = dataSourceFormat;
	}
	
	public JsonObject getModel() {
		return jsonModel;
	}
	
	public Model toRDFModel() {
		Model model = ModelFactory.createDefaultModel();
		RDFParser.fromString(jsonModel.toString())
				.lang(dataSourceFormat)
				.parse(model);
		
		return model;
	}
	
	public void addContext(List<String> context) {
		JsonArray contexts = new JsonArray();
		contexts.addAll(context.stream()
				.map(JsonString::new)
				.toList());
		jsonModel.put(NGSI_LD_CONTEXT, contexts);
	}
	
	public void setId(String entityId) {
		jsonModel.put(translateKey(NGSI_V2_KEY_ID), new JsonString(entityId));
	}
	
	public void setType(String entityType) {
		jsonModel.put(translateKey(NGSI_V2_KEY_TYPE), new JsonString(entityType));
	}
	
	public void setDateCreated(String dateCreated) {
		jsonModel.put(translateKey(NgsiV2ToLdMapping.NGSI_V2_KEY_DATE_CREATED), dateCreated);
	}
	
	public void setDateModified(String dateModified) {
		jsonModel.put(translateKey(NGSI_V2_KEY_DATE_MODIFIED), dateModified);
	}
	
	public void setDateObserved(String dateObserved) {
		jsonModel.put(NGSI_V2_KEY_DATE_OBSERVED, dateObserved);
	}
	
	public void addAttribute(String attributeKey, LinkedDataAttribute attribute) {
		jsonModel.put(translateKey(attributeKey), attribute.toAttribute());
	}
}
