package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import org.apache.jena.riot.Lang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LdesClientCli {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final LdesService ldesService;
    private final String fragmentId;
    protected Lang dataDestinationFormat;
    protected Long pollingInterval;

    public LdesClientCli(String fragmentId, Lang dataSourceFormat, Lang dataDestinationFormat, Long expirationInterval, Long pollingInterval) {
        this.dataDestinationFormat = dataDestinationFormat;
        this.pollingInterval = pollingInterval;
        this.fragmentId = fragmentId;
        ldesService = LdesClientImplFactory.getLdesService(dataSourceFormat, expirationInterval);
        ldesService.queueFragment(fragmentId);
    }

    public LdesService getService() {
        return ldesService;
    }

    public Lang getDataDestinationFormat() {
        return dataDestinationFormat;
    }

    public Long getPollingInterval() {
        return pollingInterval;
    }

    public void start() {
        CliRunner cliRunner = new CliRunner(new FragmentProcessor(ldesService, System.out, dataDestinationFormat), new EndpointChecker(fragmentId), pollingInterval);
        EXECUTOR_SERVICE.submit(cliRunner);
    }

    public static void main(String[] args) {
        LdesClientCli cli = LdesClientCliOptionParser.parseOptions(args);
        cli.start();
    }
}
