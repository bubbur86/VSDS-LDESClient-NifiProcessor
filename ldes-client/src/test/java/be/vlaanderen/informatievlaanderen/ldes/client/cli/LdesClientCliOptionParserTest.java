package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_FRAGMENT_EXPIRATION_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.junit.jupiter.api.Test;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults;
import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.LdesInvalidArgumentException;

class LdesClientCliOptionParserTest {

	private static final String FRAGMENT_ID = "https://test";

	@Test
	void whenFragmentUriIsNotProvided_cliExits() {
		String[] args = new String[] { };

		LdesInvalidArgumentException thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(args);
		});

		assertEquals(LdesClientCliOptionParser.EXIT_MISSING_FRAGMENT_ID, thrown.getExitCode());
	}

	@Test
	void whenSourceFormatIsNotProvided_LdesServiceIsConfiguredWithDefaultSourceFormat() {
		String[] args = new String[] { FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(RDFLanguages.nameToLang(DEFAULT_DATA_SOURCE_FORMAT),
				cli.getService().getDataSourceFormat());
	}

	@Test
	void whenSourceFormatIsProvided_LdesServiceIsConfiguredWithThatSourceFormat() {
		String[] args = new String[] { "-i", "turtle", FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(Lang.TURTLE, cli.getService().getDataSourceFormat());
	}

	@Test
	void whenInvalidSourceFormatIsProvided_cliExits() {
		String[] args = new String[] { "-i", "test", FRAGMENT_ID };

		LdesInvalidArgumentException thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(args);
		});

		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_SOURCE_FORMAT, thrown.getExitCode());
	}

	@Test
	void whenDestinationFormatIsNotProvided_LdesServiceIsConfiguredWithDefaultDestinationFormat() {
		String[] args = new String[] { FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(RDFLanguages.nameToLang(DEFAULT_DATA_DESTINATION_FORMAT),
				cli.getDataDestinationFormat());
	}

	@Test
	void whenDestinationFormatIsProvided_LdesServiceIsConfiguredWithThatDestinationFormat() {
		String[] args = new String[] { "-o", "turtle", FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(Lang.TURTLE, cli.getDataDestinationFormat());
	}

	@Test
	void whenInvalidDestinationFormatIsProvided_cliExits() {
		String[] args = new String[] { "-o", "test", FRAGMENT_ID };

		LdesInvalidArgumentException thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(args);
		});

		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_DESTINATION_FORMAT, thrown.getExitCode());
	}

	@Test
	void whenExpirationIntervalIsNotProvided_LdesServiceIsConfiguredWithDefaultExpirationInterval() {
		String[] args = new String[] { FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(Long.parseLong(DEFAULT_FRAGMENT_EXPIRATION_INTERVAL),
				cli.getService().getFragmentExpirationInterval());
	}

	@Test
	void whenExpirationIntervalIsProvided_LdesServiceIsConfiguredWithThatExpirationInterval() {
		String[] args = new String[] { "-e", "1", FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(1L, cli.getService().getFragmentExpirationInterval());
	}

	@Test
	void whenInvalidExpirationIntervalIsProvided_cliExits() {
		LdesInvalidArgumentException thrown;

		final String[] stringArgs = new String[] { "-e", "test", FRAGMENT_ID };
		thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(stringArgs);
		});
		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_EXPIRATION_INTERVAL, thrown.getExitCode());

		final String[] negativeArgs = new String[] { "-e", "-1", FRAGMENT_ID };
		thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(negativeArgs);
		});
		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_EXPIRATION_INTERVAL, thrown.getExitCode());
	}

	@Test
	void whenPollingIntervalIsNotProvided_LdesServiceIsConfiguredWithDefaultPollingInterval() {
		String[] args = new String[] { FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(Long.parseLong(LdesClientDefaults.DEFAULT_POLLING_INTERVAL),
				cli.getPollingInterval());
	}

	@Test
	void whenPollingIntervalIsProvided_LdesServiceIsConfiguredWithThatPollingInterval() {
		String[] args = new String[] { "-p", "1", FRAGMENT_ID };

		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);

		assertEquals(1L, cli.getPollingInterval());
	}

	@Test
	void whenInvalidPollingIntervalIsProvided_cliExits() {
		LdesInvalidArgumentException thrown;

		final String[] stringArgs = new String[] { "-p", "test", FRAGMENT_ID };
		thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(stringArgs);
		});
		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_POLLING_INTERVAL, thrown.getExitCode());

		final String[] negativeArgs = new String[] { "-p", "-1", FRAGMENT_ID };
		thrown = assertThrowsExactly(LdesInvalidArgumentException.class, () -> {
			LdesClientCliOptionParser.parseOptions(negativeArgs);
		});
		assertEquals(LdesClientCliOptionParser.EXIT_INVALID_POLLING_INTERVAL, thrown.getExitCode());
	}
}
