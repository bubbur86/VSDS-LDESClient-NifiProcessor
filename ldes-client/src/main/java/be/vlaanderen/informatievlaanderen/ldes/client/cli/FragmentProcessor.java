package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import be.vlaanderen.informatievlaanderen.ldes.client.converters.ModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class FragmentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentProcessor.class);

    private final LdesService ldesService;
    private final PrintStream printStream;
    private final Lang destinationFormat;

    public FragmentProcessor(LdesService ldesService, PrintStream printStream, Lang destinationFormat) {
        this.ldesService = ldesService;
        this.printStream = printStream;
        this.destinationFormat = destinationFormat;
    }


    public void processLdesFragments() {
        if (ldesService.hasFragmentsToProcess()) {
            LdesFragment fragment = ldesService.processNextFragment();
            LOGGER.info("Fragment {} has {} member(s)", fragment.getFragmentId(), fragment.getMembers().size());
            fragment.getMembers().forEach(member -> printStream.println(ModelConverter.convertModelToString(member.getMemberModel(), destinationFormat)));
        }
    }
}
