package be.vlaanderen.informatievlaanderen.ldes.processors.services;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowManager.class);
	
	private static int counter = 0;
	
	private FlowManager() {}
	
	public static String receiveData(ProcessSession session, FlowFile flowFile) {
		if (flowFile == null) {
			return null;
		}
		
		final AtomicReference<String> data = new AtomicReference<>();
		session.read(flowFile, in -> {
			final String contents = IOUtils.toString(in, Charset.defaultCharset());
			data.set(contents);
		});
		
		return data.get();
	}

    public static void sendRDFToRelation(ProcessSession session, FlowFile flowFile, Lang lang, String data, Relationship relationship) {
    	flowFile = session.write(flowFile, out -> { out.write(data.getBytes()); out.flush(); });
        flowFile = session.putAttribute(flowFile, CoreAttributes.MIME_TYPE.key(), lang.getContentType().toHeaderString());
        
        session.transfer(flowFile, relationship);
        
        counter++;
        LOGGER.info("TRANSFER: sent ngsi-ld quad #{} to processor {}", counter, relationship.getName());
        LOGGER.info("TRANSFER: quad data: {}", data);
    }
}
