package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesMember;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FragmentProcessorTest {
    LdesService ldesService = mock(LdesService.class);

    @Test
    void when_LdesServerHasFragments_TheyAreConvertedAndPrintedOut() throws IOException, URISyntaxException {
        when(ldesService.hasFragmentsToProcess()).thenReturn(true);
        LdesFragment ldesFragment = new LdesFragment();
        LdesMember ldesMember = readLdesMemberFromFile(getClass().getClassLoader(), "member1.txt");
        ldesFragment.addMember(ldesMember);
        when(ldesService.processNextFragment()).thenReturn(ldesFragment);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);

        FragmentProcessor fragmentProcessor = new FragmentProcessor(ldesService, printStream, Lang.NQUADS);
        fragmentProcessor.processLdesFragments();

        byteArrayOutputStream.flush();
        String actualOutput = byteArrayOutputStream.toString();
        String exptectedOutput = getExpectedOutput();
        assertTrue(convertToModel(exptectedOutput).isIsomorphicWith(convertToModel(actualOutput)));
    }

    @Test
    void when_LdesServerHasNoFragments_NothingIsPrinted() throws IOException {
        when(ldesService.hasFragmentsToProcess()).thenReturn(false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);

        FragmentProcessor fragmentProcessor = new FragmentProcessor(ldesService, printStream, null);
        fragmentProcessor.processLdesFragments();

        byteArrayOutputStream.flush();
        String actualOutput = byteArrayOutputStream.toString();
        assertEquals("", actualOutput);
    }

    private String getExpectedOutput() {
        return "<https://private-api.gipod.beta-vlaanderen.be/api/v1/organisations/9c5926df-195d-01be-a40a-360bcd21d662> <http://www.w3.org/2004/02/skos/core#prefLabel> \"Agentschap Informatie Vlaanderen\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString> .\n" +
                "<https://private-api.gipod.beta-vlaanderen.be/api/v1/organisations/9c5926df-195d-01be-a40a-360bcd21d662> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/org#Organization> .\n" +
                "<https://private-api.gipod.beta-vlaanderen.be/api/v1/taxonomies/statuses/0a4ee99b-8b8a-47c8-913f-117220febee0> <http://www.w3.org/2004/02/skos/core#prefLabel> \"In opmaak\"@nl-BE .\n" +
                "_:B867b7f08d93ed90316e3393a3247010d <http://www.w3.org/ns/adms#schemaAgency> \"https://gipod.vlaanderen.be\"@nl-BE .\n" +
                "_:B867b7f08d93ed90316e3393a3247010d <http://www.w3.org/2004/02/skos/core#notation> \"10054228\"^^<https://gipod.vlaanderen.be/ns/gipod#gipodId> .\n" +
                "_:B867b7f08d93ed90316e3393a3247010d <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/adms#Identifier> .\n" +
                "<http://localhost:10101/exampleData> <https://w3id.org/tree#member> <localhost:10101/api/v1/mobility-hindrances/10054228/165874> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/elements/1.1/creator> <https://private-api.gipod.beta-vlaanderen.be/api/v1/organisations/9c5926df-195d-01be-a40a-360bcd21d662> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://schema.org/eventSchedule> _:B3eca1fe1e968dd040375be14744a12c1 .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <https://data.vlaanderen.be/ns/mobiliteit#beheerder> <https://private-api.gipod.beta-vlaanderen.be/api/v1/organisations/9c5926df-195d-01be-a40a-360bcd21d662> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <https://data.vlaanderen.be/ns/mobiliteit#periode> _:Bf2ccfe2f3d3cab301cd55b1623205581 .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <https://data.vlaanderen.be/ns/mobiliteit#Inname.status> <https://private-api.gipod.beta-vlaanderen.be/api/v1/taxonomies/statuses/0a4ee99b-8b8a-47c8-913f-117220febee0> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/elements/1.1/contributor> <https://private-api.gipod.beta-vlaanderen.be/api/v1/organisations/9c5926df-195d-01be-a40a-360bcd21d662> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/terms/created> \"2022-04-07T18:23:24.6840261Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://www.w3.org/ns/adms#versionNotes> \"MobilityHindranceWasRegistered\"@nl-BE .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/terms/isVersionOf> <https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10054228> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://www.w3.org/ns/prov#generatedAtTime> \"2022-04-07T18:23:24.707Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/terms/modified> \"2022-04-07T18:23:24.6840261Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <https://gipod.vlaanderen.be/ns/gipod#gipodId> \"10054228\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <https://data.vlaanderen.be/ns/mobiliteit#periode> _:B30ca6afd069e9f6b807efc6d09b4d127 .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://www.w3.org/ns/adms#identifier> _:B867b7f08d93ed90316e3393a3247010d .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://data.vlaanderen.be/ns/mobiliteit#Mobiliteitshinder> .\n" +
                "<localhost:10101/api/v1/mobility-hindrances/10054228/165874> <http://purl.org/dc/terms/description> \"Description of the mobility hindrance Postman\"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/endDate> \"2023-10-17\"^^<http://www.w3.org/2001/XMLSchema#date> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/exceptDate> \"2024-02-15\"^^<http://www.w3.org/2001/XMLSchema#date> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/startTime> \"11:09Z\"^^<http://www.w3.org/2001/XMLSchema#time> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/startDate> \"2023-10-15\"^^<http://www.w3.org/2001/XMLSchema#date> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://schema.org/Schedule> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <https://data.vlaanderen.be/ns/generiek#Tijdsschema.duur> \"P2D\"^^<http://www.w3.org/2001/XMLSchema#duration> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/byMonth> \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/endTime> \"11:09Z\"^^<http://www.w3.org/2001/XMLSchema#time> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/repeatFrequency> \"P1M\"@nl-BE .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/byMonth> \"6\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/byMonth> \"10\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/repeatCount> \"12\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "_:B3eca1fe1e968dd040375be14744a12c1 <http://schema.org/byMonth> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "_:B30ca6afd069e9f6b807efc6d09b4d127 <http://data.europa.eu/m8g/startTime> \"2023-05-12T11:09:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "_:B30ca6afd069e9f6b807efc6d09b4d127 <http://data.europa.eu/m8g/endTime> \"2023-05-14T11:09:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "_:B30ca6afd069e9f6b807efc6d09b4d127 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/m8g/PeriodOfTime> .\n" +
                "_:Bf2ccfe2f3d3cab301cd55b1623205581 <http://data.europa.eu/m8g/startTime> \"2023-05-20T11:09:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "_:Bf2ccfe2f3d3cab301cd55b1623205581 <http://data.europa.eu/m8g/endTime> \"2023-05-22T11:09:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n" +
                "_:Bf2ccfe2f3d3cab301cd55b1623205581 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.europa.eu/m8g/PeriodOfTime> .\n" +
                "<https://private-api.gipod.beta-vlaanderen.be/api/v1/groundworks/103748> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://data.vlaanderen.be/ns/mobiliteit#Grondwerk> .\n" +
                "<https://private-api.gipod.beta-vlaanderen.be/api/v1/groundworks/103748> <https://data.vlaanderen.be/ns/mobiliteit#Inname.heeftGevolg> <localhost:10101/api/v1/mobility-hindrances/10054228/165874> .\n" +
                "<https://private-api.gipod.beta-vlaanderen.be/api/v1/groundworks/103748> <https://gipod.vlaanderen.be/ns/gipod#gipodId> \"103748\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n" +
                "\n";
    }

    private LdesMember readLdesMemberFromFile(ClassLoader classLoader, String fileName)
            throws URISyntaxException, IOException {
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).toURI());
        String memberString = Files.lines(Paths.get(file.toURI())).collect(Collectors.joining());
        Model outputModel = convertToModel(memberString);

        return new LdesMember("https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1",
                outputModel);
    }

    private Model convertToModel(String memberString) {
        return RDFParserBuilder.create()
                .fromString(memberString).lang(Lang.NQUADS)
                .toModel();
    }

}