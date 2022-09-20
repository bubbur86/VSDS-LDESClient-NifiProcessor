package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.StringWriter;

public class OutputFormatConverter {

    private final Lang outputFormat;

    public OutputFormatConverter(Lang outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String convertToDesiredOutputFormat(String jsonInput) {
        if (outputFormat.equals(Lang.JSONLD11)) {
            return jsonInput;
        } else {
            Model model = readJsonToModel(jsonInput);
            return writeModelToOutputFormat(model, outputFormat);
        }
    }

    private Model readJsonToModel(String jsonInput) {
        return RDFParserBuilder.create()
                .fromString(jsonInput).lang(Lang.JSONLD11)
                .toModel();
    }

    private String writeModelToOutputFormat(final Model model, final Lang lang) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, lang);
        return stringWriter.toString();
    }
}
