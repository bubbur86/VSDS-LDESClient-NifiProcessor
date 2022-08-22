package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliRunner.class);
    private boolean threadrunning = true;
    private boolean endpointAvailable = false;
    private final EndpointChecker endpointChecker;
    private final FragmentProcessor fragmentProcessor;
    private final long endpointPollingInterval;

    public CliRunner(FragmentProcessor fragmentProcessor, EndpointChecker endpointChecker, long endpointPollingInterval) {
        this.fragmentProcessor = fragmentProcessor;
        this.endpointChecker = endpointChecker;
        this.endpointPollingInterval = endpointPollingInterval;
    }

    @Override
    public void run() {
        while (threadrunning) {
            if (!isEndpointAlreadyAvailable()) {
                waitUntilEndpointBecomesAvailable();
            } else {
                fragmentProcessor.processLdesFragments();
            }
        }
    }

    private boolean isEndpointAlreadyAvailable() {
        if (!endpointAvailable && endpointChecker.isReachable()) {
            endpointAvailable = true;
        }
        return endpointAvailable;
    }

    private void waitUntilEndpointBecomesAvailable() {
        try {
            Thread.sleep(endpointPollingInterval * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted thread", e);
        }
    }

    public void setThreadrunning(boolean threadrunning) {
        this.threadrunning = threadrunning;
    }
}
