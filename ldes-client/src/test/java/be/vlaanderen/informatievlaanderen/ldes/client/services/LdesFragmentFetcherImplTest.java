package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

@WireMockTest(httpPort = 10101)
class LdesFragmentFetcherImplTest {

	private final String initialFragmentUrl = "http://localhost:10101/exampleData";
	private final String actualFragmentUrl = "http://localhost:10101/exampleData?generatedAtTime=2022-05-05T00:00:00.000Z";

	private LdesService ldesService;

	@Test
	void whenFragmentUrlRedirects_thenFragmentIdWillBeSetToTargetUrl() {
		ldesService = LdesClientImplFactory.getLdesService(Lang.JSONLD11);

		ldesService.queueFragment(initialFragmentUrl);

		LdesFragment fragment = ldesService.processNextFragment();

		assertEquals(actualFragmentUrl, fragment.getFragmentId());
	}
}
