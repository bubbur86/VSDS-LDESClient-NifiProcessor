package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.valueobjects.MemberInfo;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;

public class OutputFormatConverter {

    private static final String PROV_GENERATED_AT_TIME = "http://www.w3.org/ns/prov#generatedAtTime";
    private static final String XMLSCHEMA_DATE_TIME = "https://www.w3.org/2001/XMLSchema#DateTime";

    private final Lang outputFormat;
    private final boolean addTopLevelGeneratedAt;

    public OutputFormatConverter(Lang outputFormat, boolean addTopLevelGeneratedAt) {
        this.outputFormat = outputFormat;
        this.addTopLevelGeneratedAt = addTopLevelGeneratedAt;
    }

    public String convertToDesiredOutputFormat(String jsonInput, MemberInfo memberInfo) {
        Model model = readJsonToModel(jsonInput);
        addAdditionalStatements(memberInfo, model);
        return writeModelToOutputFormat(model, outputFormat);

    }

    private void addAdditionalStatements(MemberInfo memberInfo, Model model) {
        Resource resource = model.listSubjects().filterKeep(subject -> !subject.isAnon()).nextOptional().orElseThrow(RuntimeException::new);
        List<Statement> statements = new ArrayList<>();
        if (addTopLevelGeneratedAt)
            statements.add(createStatement(resource, createProperty(PROV_GENERATED_AT_TIME), createTypedLiteral(memberInfo.getObservedAt(),
                    TypeMapper.getInstance().getTypeByName(XMLSCHEMA_DATE_TIME))));
        model.add(statements);
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
