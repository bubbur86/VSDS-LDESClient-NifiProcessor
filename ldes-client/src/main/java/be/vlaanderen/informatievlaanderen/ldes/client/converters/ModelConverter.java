package be.vlaanderen.informatievlaanderen.ldes.client.converters;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;

import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

public class ModelConverter {
	
	private ModelConverter() {}
	
	public static String convertModelToString(Model model) {
		return convertModelToString(model, RDFLanguages.nameToLang(DEFAULT_DATA_DESTINATION_FORMAT));
	}

	public static String convertModelToString(Model model, Lang lang) {
		StringWriter out = new StringWriter();
		
		RDFDataMgr.write(out, model, lang);
		
		return out.toString();
	}
}
