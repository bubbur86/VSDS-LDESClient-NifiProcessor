package be.vlaanderen.informatievlaanderen.ldes.client.cli;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointCheckerTest {


    @ParameterizedTest(name = "endpoint {0} is reachable: {1}")
    @ArgumentsSource(ContentTypeRdfFormatLangArgumentsProvider.class)
    void when_EndpointCanBeReached_IsReachableIsTrue(String endpoint, boolean isReachable) {
        EndpointChecker endpointChecker = new EndpointChecker(endpoint);
        assertEquals(isReachable, endpointChecker.isReachable());
    }

    static class ContentTypeRdfFormatLangArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("http://www.google.be", true),
                    Arguments.of("http://www.not-existing-site.be", false));
        }
    }

}