package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesStateManager;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

class LdesStateManagerTest {
    LdesStateManager stateManager;
    Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    
    LocalDateTime fragmentExpirationDate = LocalDateTime.now().plusSeconds(3600L);
    
    LdesFragment fragmentToProcess = new LdesFragment("localhost:10101/testData?1", fragmentExpirationDate);
    LdesFragment nextFragmentToProcess = new LdesFragment("localhost:10101/testData?2", fragmentExpirationDate);

    String memberIdToProcess = "localhost:10101/api/v1/data/10228974/2397";

    @BeforeEach
    public void init() {
        stateManager = new LdesStateManager(3600L);
        stateManager.queueFragment(fragmentToProcess.getFragmentId());
    }

    @Test
    void when_StateManagerIsInitialized_QueueHasOnlyOneItemAndReturnsNullOtherwise() {
        assertTrue(stateManager.hasNext());
        assertEquals(fragmentToProcess.getFragmentId(), stateManager.next());

        assertFalse(stateManager.hasNext());
        assertNull(stateManager.next());
    }

    @Test
    void when_tryingToQueueSameFragmentTwice_FragmentDoesNotGetAddedToQueue() {
        assertTrue(stateManager.hasNext());
        assertEquals(1, stateManager.countQueuedFragments());
        stateManager.queueFragment(fragmentToProcess.getFragmentId());
        assertEquals(1, stateManager.countQueuedFragments());
        
        String nextFragment = stateManager.next();
        assertEquals(fragmentToProcess.getFragmentId(), nextFragment);
        stateManager.processedFragment(new LdesFragment(nextFragment, null));
        
        stateManager.queueFragment(nextFragmentToProcess.getFragmentId());

        assertEquals(nextFragmentToProcess.getFragmentId(), stateManager.next());
        assertFalse(stateManager.hasNext());
    }
    
    @Test
    void when_queueingAndProcessingMultipleFragments_queueIsAsExpected() {
        String nextFragment;
        
        nextFragment = stateManager.next();
        assertEquals(fragmentToProcess.getFragmentId(), nextFragment);
        stateManager.processedFragment(new LdesFragment(nextFragment, null));
        
        stateManager.queueFragment(nextFragmentToProcess.getFragmentId());
        
        nextFragment = stateManager.next();
        assertEquals(nextFragmentToProcess.getFragmentId(), nextFragment);
        
        assertFalse(stateManager.hasNext());
    	
    }

    @Test
     void when_tryingToProcessAnAlreadyProcessedLdesMember_MemberDoesNotGetProcessed() {
    	assertTrue(stateManager.shouldProcessMember(fragmentToProcess, memberIdToProcess));
        assertFalse(stateManager.shouldProcessMember(fragmentToProcess, memberIdToProcess));
    }

    @Test
    void when_parsingImmutableFragment_saveAsProcessedPageWithEmptyExpireDate() {
    	fragmentToProcess.setImmutable(true);
    	
    	assertEquals(0, stateManager.countProcessedImmutableFragments());
        stateManager.processedFragment(fragmentToProcess);
        assertEquals(1, stateManager.countProcessedImmutableFragments());
    }

    @Test
    void when_afterFirstProcessing_fragmentIsEitherMutableOrImmutable() {
        fragmentToProcess.setImmutable(true);

        stateManager.processedFragment(fragmentToProcess);

        boolean isFragmentProcessed = stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId())
                || stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId());
        
        assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
        assertTrue(isFragmentProcessed);
    }

    @Test
    void when_processedFragmentIsImmutable_isContainedInImmutableFragmentQueue() {
        fragmentToProcess.setImmutable(true);

        stateManager.processedFragment(fragmentToProcess);
        
        assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
        assertTrue(stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId()));
        assertFalse(stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId()));
    }

    @Test
    void when_processedFragmentIsMutable_isContainedInMutableFragmentQueue() {
        fragmentToProcess.setImmutable(false);

        stateManager.processedFragment(fragmentToProcess);
        
        assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
        assertFalse(stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId()));
        assertTrue(stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId()));
    }

    @Test
    void when_onlyImmutableFragments_QueueRemainsEmpty() {
    	fragmentToProcess.setImmutable(true);
    	stateManager.processedFragment(fragmentToProcess);

        assertFalse(stateManager.hasNext());
    }
}
