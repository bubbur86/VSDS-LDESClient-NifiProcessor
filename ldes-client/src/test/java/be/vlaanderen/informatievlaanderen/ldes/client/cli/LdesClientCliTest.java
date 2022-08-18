package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFParserBuilder;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@WireMockTest(httpPort = 10101)
class LdesClientCliTest {

	private final String cliOutputFragmentUrl = "http://localhost:10101/cli-output-test";

	Properties properties;
	LdesClientCli cli;

	String member1File = "src/test/resources/member1.txt";
	Model member1;
	String member1CompareFile = "src/test/resources/member1_cmp.txt";
	Model member1Compare;

	String member2File = "src/test/resources/member2.txt";
	Model member2;

	@BeforeEach
	private void setup() throws Exception {
		properties = new Properties();
		properties.load(new FileInputStream(new File("src/test/resources/ldesclientcli-test.properties")));
		cli = new LdesClientCli(cliOutputFragmentUrl);

		member1 = RDFParserBuilder.create().fromString(Files.readString(Path.of(member1File))).forceLang(cli.dataDestinationFormat).toModel();
		member1Compare = RDFParserBuilder.create().fromString(Files.readString(Path.of(member1CompareFile))).forceLang(cli.dataDestinationFormat).toModel();
		member2 = RDFParserBuilder.create().fromString(Files.readString(Path.of(member2File))).forceLang(cli.dataDestinationFormat).toModel();
	}

	@Test
	void whenCliIsStarted_PropertiesAreLoaded() throws Exception {
		assertEquals("100", properties.getProperty("ldes.client.cli.polling.interval"));
		assertEquals(30, cli.pollingInterval);
	}
	
	@Test
	void when2MembersOnlyDifferInAnonymousNodeNames_membersAreIsoMorphic() throws Exception {
		assertTrue(member1.isIsomorphicWith(member1Compare));
	}

	@Test
	void whenLdesProcessingIsStarted_membersArePrintedToTheConsole() throws Exception {
		String consoleOutput = captureOutput();
		
		assertNotNull(consoleOutput);
		assertTrue(consoleOutput.trim().length() > 0);
		
		String[] data = consoleOutput.split("\n\n");
		for (String memberData : data) {
			assertNotEquals("", memberData.trim());
		}
	}
	
	@Test
	void whenMembersAreOutputted_memberModelsAreIsomorphic() throws Exception {
		String consoleOutput = captureOutput();
		
		String[] data = consoleOutput.split("\n\n");
		
		assertEquals(1, data.length);
		
		for (String memberData : data) {
			Model model = RDFParserBuilder.create()
				.fromString(memberData.trim())
				.forceLang(cli.dataDestinationFormat)
				.toModel();

			assertTrue(member1.isIsomorphicWith(model));
		}
	}
	
	private String captureOutput() throws Exception {
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		PrintStream print = new PrintStream(outContent);

		cli.setOutputStream(print);
		cli.start();

		
		while (!cli.isWaiting()) {
			await()
				.pollDelay(Duration.ONE_SECOND)
                .atMost(Duration.FIVE_SECONDS)
                .until(cli::isWaiting);
		}
		
		cli.stop();

		print.flush();

		return outContent.toString().trim();
	}
}