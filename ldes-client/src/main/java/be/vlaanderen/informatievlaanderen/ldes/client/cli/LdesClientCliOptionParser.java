package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_DESTINATION_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_DATA_SOURCE_FORMAT;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_FRAGMENT_EXPIRATION_INTERVAL;
import static be.vlaanderen.informatievlaanderen.ldes.client.LdesClientDefaults.DEFAULT_POLLING_INTERVAL;

import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.LdesInvalidArgumentException;

public class LdesClientCliOptionParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdesClientCliOptionParser.class);
	
	private LdesClientCliOptionParser() {}
	
	private static void printHelp(Options options) {
		String header = "Replicate and synchronize an LDES from the command line";
		String footer = "Please report issues at https://github.com/Informatievlaanderen/VSDS-LDESClient4J";
		
		HelpFormatter help = new HelpFormatter();
		
		help.setWidth(200);
		help.printHelp("java -jar ldes-client-1.0-SNAPSHOT-jar-with-dependencies.jar [OPTIONS] <FRAGMENT URI>", header, options, footer, false);
		
		System.exit(0);
	}
	
	private static void log(Options options, String message, Object... args) {
		LOGGER.error(message, args);
		System.err.println("ERROR: " + message + " " + args + System.lineSeparator());
		printHelp(options);
	}
	
	private static Option createOption(String name, String argName, String description, String longOpt, boolean required, boolean hasArg) {
		return Option.builder(name)
				.argName(argName)
				.desc(description)
				.hasArg(hasArg)
				.longOpt(longOpt)
				.required(required)
				.build();
	}
	
	private static String parseOption(CommandLine cmd, Options options, Option option) {
		if (cmd.hasOption(option)) {
			return cmd.getOptionValue(option);
		}
		
		if (option.isRequired()) {
			log(options, "Missing required option {}", option);
		}
		
		return null;
	}
	
	private static Lang parseLang(CommandLine cmd, Options options, Option option, String description, String defaultValue) {
		String input = parseOption(cmd, options, option);
		
		if (input == null) {
			input = defaultValue;
		}
		
		Lang lang = RDFLanguages.nameToLang(input);
		
		if (lang == null) {
			log(options, "Not a valid org.apache.jena.riot.Lang for {}: {}", description, option);
		}
		
		return lang;
	}
	
	private static Long parseLong(CommandLine cmd, Options options, Option option, String description, String defaultValue) {
		String input = parseOption(cmd, options, option);
		
		if (input == null) {
			input = defaultValue;
		}
		
		try {
			Long value = Long.parseLong(input);
			
			if (value < 0L) {
				log(options, "Must be a positive integer or long {}: {}", description, option);
			}
			
			return value;
		}
		catch (NumberFormatException e) {
			log(options, "Not valid (must be a positive integer or long) {}: {}", description.toLowerCase(), input);
		}
		
		return null;
	}

	public static LdesClientCli parseOptions(String[] args) {
		String fragmentId = null;
		Lang dataSourceFormat = null;
		Lang dataDestinationFormat = null;
		Long expirationInterval = null;
		Long pollingInterval = null;
		
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();

		Option optionInputFormat = createOption("i", "org.apache.jena.riot.Lang", "Format of the LDES source (e.g. n-quads, json-ld)", "input-format", false, true);
		Option optionOutputFormat = createOption("o", "org.apache.jena.riot.Lang","Format of the outputted members (e.g. n-quads, json-ld)", "output-format", false, true);
		Option optionExpirationInterval = createOption("e", "seconds", "Number of seconds to expire an unconfigured mutable fragment", "expiration", false, true);
		Option optionPollingInterval = createOption("p", "seconds", "Number of seconds to wait before polling the data source again", "polling", false, true);
		Option optionHelp = createOption("?", null, "Prints an informative help message", "help", false, false);
		
		options.addOption(optionInputFormat);
		options.addOption(optionOutputFormat);
		options.addOption(optionExpirationInterval);
		options.addOption(optionPollingInterval);
		options.addOption(optionHelp);

		try {
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption(optionHelp)) {
				printHelp(options);
			}
			
			dataSourceFormat = parseLang(cmd, options, optionInputFormat, "input format", DEFAULT_DATA_SOURCE_FORMAT);
			dataDestinationFormat = parseLang(cmd, options, optionOutputFormat, "output format", DEFAULT_DATA_DESTINATION_FORMAT);
			expirationInterval = parseLong(cmd, options, optionExpirationInterval, "Expiration interval", DEFAULT_FRAGMENT_EXPIRATION_INTERVAL);
			pollingInterval = parseLong(cmd, options, optionPollingInterval, "Polling interval", DEFAULT_POLLING_INTERVAL);
			
			LOGGER.info("Parsed options: dataSourceFormat {}, dataDestinationFormat {}, expirationInterval {}, pollingInterval {}", dataSourceFormat, dataDestinationFormat, expirationInterval, pollingInterval);
			
			args = cmd.getArgs();
			if (args.length == 0) {
				log(options, "Fragment uri must be provided");
			}

			fragmentId = args[0];
			if (fragmentId.trim().length() == 0) {
				log(options, "The base fragment uri for the LDES to follow must be provided");
			}
			
			URI fragmentUri = URI.create(fragmentId);
			fragmentId = fragmentUri.toString();

			LOGGER.info("Retrieving fragments from {}", fragmentId);

			return new LdesClientCli(fragmentId, dataSourceFormat, dataDestinationFormat, expirationInterval, pollingInterval);
		} catch (MissingOptionException m) {
			log(options, "Missing required option(s)", m);
		} catch (ParseException p) {
			log(options, "Exception while parsing command line arguments", p);
		} catch (NullPointerException | IllegalArgumentException u) {
			log(options, "The base fragment uri for the LDES to follow must be provided and be a valid URI");
		}
		
		throw new LdesInvalidArgumentException("An error occurred while parsing options and setting up a CLI");
	}
}
