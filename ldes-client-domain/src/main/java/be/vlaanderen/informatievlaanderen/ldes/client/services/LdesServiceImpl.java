package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesConstants.W3ID_TREE_MEMBER;
import static be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesConstants.W3ID_TREE_NODE;
import static be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesConstants.W3ID_TREE_RELATION;
import static be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesConstants.W3ID_TREE_VIEW;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.TripleBoundary;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelExtract;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StatementTripleBoundary;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.LdesStateManager;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesMember;

public class LdesServiceImpl implements LdesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdesServiceImpl.class);
	
    protected static final Resource ANY_RESOURCE = null;
    protected static final Property ANY_PROPERTY = null;
	
	public static final String DEFAULT_DATA_SOURCE_FORMAT = "json-ld";
	public static final String DEFAULT_DATA_DESTINATION_FORMAT = "quads";
	public static final Long DEFAULT_FRAGMENT_EXPIRATION_INTERVAL = 3600L;
	
	protected final LdesStateManager stateManager;
    private final ModelExtract modelExtract;
    protected final LdesFragmentFetcher fragmentFetcher;
    private final Lang dataDestinationFormat;

	/**
	 * Replicates and synchronizes an LDES data set.
	 * 
	 * @param dataSourceUrl the base url of the data set
	 * @param lang          the data format the data set is returned in (e.g.
	 *                      JSONLD11, N-QUADS)
	 */
	public LdesServiceImpl() {
		this(RDFLanguages.nameToLang(DEFAULT_DATA_SOURCE_FORMAT), RDFLanguages.nameToLang(DEFAULT_DATA_DESTINATION_FORMAT));
	}
	
	public LdesServiceImpl(Lang dataFormat) {
		this (dataFormat, dataFormat);
	}
	
	public LdesServiceImpl(Lang dataSourceFormat, Lang dataDestinationFormat) {
		this(dataSourceFormat, dataDestinationFormat, DEFAULT_FRAGMENT_EXPIRATION_INTERVAL);
	}

	/**
	 * Replicates and synchronizes an LDES data set.
	 * 
	 * The defaultExpirationInterval will be set on fragments that do not have a value for max-age
	 * in the Cache-control header.
	 * 
	 * @param dataSourceUrl the base url of the data set
	 * @param dataSourceFormat          the expected data format of the data source (e.g. JSONLD11, N-QUADS)
	 * @param dataDestinationFormat		the desired data format for the extracted and processed data (e.g. JSONLD11, N-QUADS)
	 * @param defaultFragmentExpirationInterval	the amount of seconds to add to LocalDateTime.now() before the mutable fragment is considered to be expired.
	 */
	public LdesServiceImpl(Lang dataSourceFormat, Lang dataDestinationFormat, Long defaultFragmentExpirationInterval) {
		this.dataDestinationFormat = dataDestinationFormat;
		
		stateManager = new LdesStateManager(defaultFragmentExpirationInterval);
		modelExtract = new ModelExtract(new StatementTripleBoundary(TripleBoundary.stopNowhere));
		fragmentFetcher = LdesClientImplFactory.getFragmentFetcher(dataSourceFormat);
	}
	
	@Override
	public LdesStateManager getStateManager() {
		return stateManager;
	}
	
	@Override
	public void queueFragment(String fragmentId) {
		queueFragment(fragmentId, null);
	}
	
	@Override
	public void queueFragment(String fragmentId, LocalDateTime expirationDate) {
		stateManager.queueFragment(fragmentId, expirationDate);
	}
	
	@Override
	public boolean hasFragmentsToProcess() {
		return stateManager.hasNext();
	}

	@Override
	public LdesFragment processNextFragment() {
		LdesFragment fragment = fragmentFetcher.fetchFragment(stateManager.next());

		// Extract and process the members and add them to the fragment
		extractMembers(fragment.getModel(), fragment.getFragmentId())
				.forEach(memberStatement -> {
					if (stateManager.shouldProcessMember(fragment, memberStatement.getObject().toString())) {
						fragment.addMember(processMember(fragment, memberStatement));
					}
				});
		
//		LOGGER.info("PROCESSING NEXT FRAGMENT {} with {} member(s): {}",
//				fragment.getFragmentId(),
//				fragment.getMembers().size(),
//				fragment.getMembers()
//				.stream()
//					.map(member -> member.getMemberId())
//					.collect(Collectors.joining(", ")));

		// Extract relations and add them to the fragment
		extractRelations(fragment.getModel()).forEach(relationStatement -> fragment
				.addRelation(relationStatement.getResource().getProperty(W3ID_TREE_NODE).getResource().toString()));
		// Queue the related fragments
		fragment.getRelations().forEach(fragmentId -> stateManager.queueFragment(fragmentId));

		// Inform the StateManager that a fragment has been processed
		stateManager.processedFragment(fragment);

		LOGGER.info("PROCESSED fragment {} ({}MUTABLE) has {} member(s) and {} tree:relation(s)", fragment.getFragmentId(),
				fragment.isImmutable() ? "IM" : "", fragment.getMembers().size(), fragment.getRelations().size());

		return fragment;
	}

	protected Stream<Statement> extractMembers(Model fragmentModel, String fragmentId) {
		Resource subjectId = fragmentModel
				.listStatements(ANY_RESOURCE, W3ID_TREE_VIEW, fragmentModel.createResource(fragmentId))
				.toList()
				.stream()
				.findFirst()
				.map(Statement::getSubject)
				.orElse(null);
		StmtIterator memberIterator = fragmentModel.listStatements(subjectId, W3ID_TREE_MEMBER, ANY_RESOURCE);

		return Stream.iterate(memberIterator, Iterator<Statement>::hasNext, UnaryOperator.identity())
				.map(Iterator::next);
	}

	protected LdesMember processMember(LdesFragment fragment, Statement memberStatement) {
		Model fragmentModel = fragment.getModel();
		Model memberModel = modelExtract.extract(memberStatement.getObject().asResource(), fragmentModel);
		String memberId = memberStatement.getObject().toString();

		memberModel.add(memberStatement);

		// Add reverse properties
		Set<Statement> otherLdesMembers = fragmentModel
				.listStatements(memberStatement.getSubject(), W3ID_TREE_MEMBER, ANY_RESOURCE)
				.toSet()
				.stream()
				.filter(statement -> !memberStatement.equals(statement))
				.collect(Collectors.toSet());

		fragmentModel.listStatements(ANY_RESOURCE, ANY_PROPERTY, memberStatement.getResource())
				.filterKeep(statement -> statement.getSubject().isURIResource())
				.filterDrop(memberStatement::equals)
				.forEach(statement -> {
					Model reversePropertyModel = modelExtract.extract(statement.getSubject(), fragmentModel);
					List<Statement> otherMembers = reversePropertyModel
							.listStatements(statement.getSubject(), statement.getPredicate(), ANY_RESOURCE).toList();
					otherLdesMembers.forEach(otherLdesMember -> {
						reversePropertyModel.listStatements(ANY_RESOURCE, ANY_PROPERTY, otherLdesMember.getResource()).toList();
					});
					otherMembers.forEach(otherMember -> {
						reversePropertyModel.remove(modelExtract.extract(otherMember.getResource(), reversePropertyModel));
					});

					memberModel.add(reversePropertyModel);
				});

		StringWriter outputStream = new StringWriter();
		
		RDFDataMgr.write(outputStream, memberModel, dataDestinationFormat);

		LOGGER.info("PROCESSED LDES member ({}) on fragment {}", memberId, fragment.getFragmentId());
		
		return new LdesMember(memberId, outputStream.toString());
	}

	@Override
	public Stream<Statement> extractRelations(Model fragmentModel) {
		return Stream.iterate(fragmentModel.listStatements(ANY_RESOURCE, W3ID_TREE_RELATION, ANY_RESOURCE),
				Iterator<Statement>::hasNext, UnaryOperator.identity()).map(Iterator::next);
	}
}