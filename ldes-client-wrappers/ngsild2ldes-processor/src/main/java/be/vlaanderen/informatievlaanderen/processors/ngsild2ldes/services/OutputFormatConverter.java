package be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.services;

import be.vlaanderen.informatievlaanderen.processors.ngsild2ldes.valueobjects.MemberInfo;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.*;
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
    private static final String WKT_DATA_TYPE = "http://www.opengis.net/ont/geosparql#wktLiteral";
    private static final String GEOSPARQL_AS_WKT = "http://www.opengis.net/ont/geosparql#asWKT";

    private final WKTExtractor wktExtractor = new WKTExtractor();
    private final Lang outputFormat;
    private final boolean addTopLevelGeneratedAt;
    private boolean addWKTProperty;

    public OutputFormatConverter(Lang outputFormat, boolean addTopLevelGeneratedAt, boolean addWKTProperty) {
        this.outputFormat = outputFormat;
        this.addTopLevelGeneratedAt = addTopLevelGeneratedAt;
        this.addWKTProperty = addWKTProperty;
    }

    public String convertToDesiredOutputFormat(String jsonInput, MemberInfo memberInfo) {

        Model model = readJsonToModel(jsonInput);
        String wkt = wktExtractor.extractWKT(jsonInput);
        addAdditionalStatements(memberInfo, model, wkt);
        return writeModelToOutputFormat(model, outputFormat);

    }

    private void addAdditionalStatements(MemberInfo memberInfo, Model model, String wkt) {
        Resource resource = model.listSubjects().filterKeep(subject -> !subject.isAnon()).nextOptional().orElseThrow(RuntimeException::new);
        List<Statement> statements = new ArrayList<>();
        if (addWKTProperty && wkt != null)
            statements.add(createStatement(resource, createProperty(GEOSPARQL_AS_WKT), createTypedLiteral(wkt, TypeMapper.getInstance().getTypeByName(WKT_DATA_TYPE))));
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
