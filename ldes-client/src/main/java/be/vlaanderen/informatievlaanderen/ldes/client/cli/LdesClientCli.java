package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_FRAGMENT_EXPIRATION_INTERVAL;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_POLLING_INTERVAL;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.converters.ModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

public class LdesClientCli implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdesClientCli.class);

	private static final String PROPERTIES_FILE = "ldesclientcli.properties";

	private static final String DATA_SOURCE_FORMAT = "ldes.client.cli.data.source.format";
	private static final String DATA_DESTINATION_FORMAT = "ldes.client.cli.data.destination.format";
	private static final String EXPIRATION_INTERVAL = "ldes.client.cli.fragment.expiration.interval";
	private static final String POLLING_INTERVAL = "ldes.client.cli.polling.interval";

	private final LdesService ldesService;

	protected Lang dataSourceFormat;
	protected Lang dataDestinationFormat;
	protected Long expirationInterval;
	protected Long pollingInterval;

	private PrintStream out;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean waiting = new AtomicBoolean(false);

	public LdesClientCli(String fragmentId) {
		this(fragmentId, PROPERTIES_FILE);
	}

	public LdesClientCli(String fragmentId, String propertiesFile) {
		this(fragmentId, loadProperties(propertiesFile));
	}
	
	public LdesClientCli(String fragmentId, Properties properties) {
		this(fragmentId,
				RDFLanguages.nameToLang(properties.getProperty(DATA_SOURCE_FORMAT, DEFAULT_DATA_SOURCE_FORMAT)),
				RDFLanguages.nameToLang(properties.getProperty(DATA_DESTINATION_FORMAT, DEFAULT_DATA_DESTINATION_FORMAT)),
				Long.parseLong(properties.getProperty(EXPIRATION_INTERVAL, DEFAULT_FRAGMENT_EXPIRATION_INTERVAL)),
				Long.parseLong(properties.getProperty(POLLING_INTERVAL, DEFAULT_POLLING_INTERVAL)));
	}
	
	public LdesClientCli(String fragmentId, Lang dataSourceFormat, Lang dataDestinationFormat, Long expirationInterval, Long pollingInterval) {
		this.dataSourceFormat = dataSourceFormat;
		this.dataDestinationFormat = dataDestinationFormat;
		this.expirationInterval = expirationInterval;
		this.pollingInterval = pollingInterval;

		LOGGER.info("Starting LDES Client CLI with properties from {}: dataSourceFormat {}, dataDestinationFormat {}, expirationInterval {}, pollingInterval {}", PROPERTIES_FILE, dataSourceFormat, dataDestinationFormat, expirationInterval, pollingInterval);

		ldesService = LdesClientImplFactory.getLdesService(dataSourceFormat, expirationInterval);

		ldesService.queueFragment(fragmentId);

		setOutputStream(System.out);
	}

	protected void setOutputStream(PrintStream out) {
		this.out = out;
	}

	private static Properties loadProperties(String propertiesFile) {
		try(InputStream input = LdesClientCli.class.getClassLoader().getResourceAsStream(propertiesFile)) {
			Properties properties = new Properties();

			properties.load(input);

			return properties;
		} catch (IOException e) {
			LOGGER.error("Error while loading properties file {}", propertiesFile);

			System.exit(1);
		}

		throw new IllegalArgumentException("Unable to load properties from file " + propertiesFile);
	}

	public void start() {
		Thread runner = new Thread(this);
		runner.start();
	}

	public void stop() {
		running.set(false);
		waiting.set(false);
	}

	public void run() {
		LOGGER.info("Scheduled fragment polling with interval {}s", pollingInterval);
		running.set(true);

		while (running.get()) {
			while (ldesService.hasFragmentsToProcess()) {
				LdesFragment fragment = ldesService.processNextFragment();

				LOGGER.info("Fragment {} has {} member(s)", fragment.getFragmentId(), fragment.getMembers().size());
				fragment.getMembers().forEach(member -> out.println(ModelConverter.convertModelToString(member.getMemberModel(), dataDestinationFormat)));
			}

			waiting.set(true);
			try {
				Thread.sleep(pollingInterval * 1000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
	
				LOGGER.error("Interrupted thread", e);
			}
			waiting.set(false);
		}

		LOGGER.info("Stopped polling for fragments");
	}

	public boolean isStarted() {
		return running.get();
	}

	public boolean isStopped() {
		return running.get();
	}

	public boolean isWaiting() {
		return waiting.get();
	}

	public boolean isActive() {
		return !isWaiting();
	}

	public static void main(String[] args) {
		LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);
		cli.start();
	}
}
