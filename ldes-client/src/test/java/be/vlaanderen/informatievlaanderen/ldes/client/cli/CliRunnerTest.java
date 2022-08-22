package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CliRunnerTest {

    @Test
    void when_CliRunnerIsRunning_EndpointIsBeingChecked() {
        Awaitility.reset();
        EndpointChecker endpointChecker = mock(EndpointChecker.class);
        FragmentProcessor fragmentProcessor = mock(FragmentProcessor.class);
        CliRunner cliRunner = new CliRunner(fragmentProcessor, endpointChecker, 1);

        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> submit = service.submit(cliRunner);
        assertFalse(submit.isDone());

        await()
                .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
                .atMost(2, TimeUnit.SECONDS)
                .until(()->mockingDetails(endpointChecker).getInvocations().size()==2);
        cliRunner.setThreadrunning(false);
        await()
                .pollDelay(Durations.ONE_HUNDRED_MILLISECONDS)
                .atMost(Durations.FIVE_SECONDS)
                .until(submit::isDone);
        assertTrue(submit.isDone());
        verify(endpointChecker, times(2)).isReachable();
    }

    @Test
    void when_EndpointIsNotReachable_ItIsPeriodicallyPolled() {
        Awaitility.reset();
        EndpointChecker endpointChecker = mock(EndpointChecker.class);
        FragmentProcessor fragmentProcessor = mock(FragmentProcessor.class);
        when(endpointChecker.isReachable()).thenReturn(false);

        CliRunner cliRunner = new CliRunner(fragmentProcessor, endpointChecker, 1);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(cliRunner);

        await()
                .during(10, TimeUnit.MILLISECONDS)
                .until(()->true);
        cliRunner.setThreadrunning(false);

        verifyNoInteractions(fragmentProcessor);
    }

    @Test
    void when_EndpointIsReachable_FragmentProcessorIsCalled() {
        Awaitility.reset();
        EndpointChecker endpointChecker = mock(EndpointChecker.class);
        FragmentProcessor fragmentProcessor = mock(FragmentProcessor.class);
        when(endpointChecker.isReachable()).thenReturn(true);

        CliRunner cliRunner = new CliRunner(fragmentProcessor, endpointChecker, 1);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(cliRunner);

        await()
                .during(10, TimeUnit.MILLISECONDS)
                .until(()->true);
        cliRunner.setThreadrunning(false);

        verify(fragmentProcessor, atLeast(1)).processLdesFragments();
    }

}