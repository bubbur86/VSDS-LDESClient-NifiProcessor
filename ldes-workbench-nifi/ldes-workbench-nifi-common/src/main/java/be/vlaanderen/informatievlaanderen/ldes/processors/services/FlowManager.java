package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import be.vlaanderen.informatievlaanderen.ldes.processors.exceptions.ContentRetrievalException;
import org.apache.jena.riot.Lang;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class FlowManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);

	private static int counter = 0;

	private FlowManager() {}

	public static String receiveData(ProcessSession session, FlowFile flowFile) {
		return receiveData(session, flowFile, new ByteArrayOutputStream());
	}
	
	public static String receiveData(ProcessSession session, FlowFile flowFile, ByteArrayOutputStream baos) {
		if (flowFile == null) {
			return null;
		}

		try {
			session.exportTo(flowFile, baos);
			baos.close();
			
			return baos.toString();
		}
		catch (Exception e) {
			throw new ContentRetrievalException(flowFile.getId(), e);
		}
	}

    public static void sendRDFToRelation(ProcessSession session, Lang lang, String memberData, Relationship relationship) {
    	sendRDFToRelation(session, lang, memberData, relationship, session.create());
    }

	public static void sendRDFToRelation(ProcessSession session, Lang lang, String memberData, Relationship relationship, FlowFile flowFile) {

		flowFile = session.write(flowFile, out -> out.write(memberData.getBytes()));
		flowFile = session.putAttribute(flowFile, CoreAttributes.MIME_TYPE.key(), lang.getContentType().toHeaderString());

		session.transfer(flowFile, relationship);

		counter++;
		LOGGER.info("TRANSFER: sent quad #{} to processor {}", counter, relationship.getName());
		LOGGER.info("TRANSFER: quad data: {}", memberData);
	}
}
