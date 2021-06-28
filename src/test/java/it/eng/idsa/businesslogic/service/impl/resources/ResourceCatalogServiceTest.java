package it.eng.idsa.businesslogic.service.impl.resources;

import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.fraunhofer.iais.eis.AudioResourceBuilder;
import de.fraunhofer.iais.eis.DataResourceBuilder;
import de.fraunhofer.iais.eis.ImageResourceBuilder;
import de.fraunhofer.iais.eis.RepresentationBuilder;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.SoftwareResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.resources.ResourceCatalogService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.util.DateUtil;

public class ResourceCatalogServiceTest {

	private ResourceCatalogService service;
	
	private ObjectMapper mapper;
	
	
	@BeforeEach
	public void setup() {
		mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
		module.addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
		mapper.registerModule(module);
	}
	
	@Test
	public void asdasd() {
		System.out.println(getObjAsString(getResourceCatalog()));
		
//		System.out.println(getObjAsString(getResource()));
		
		
//		System.out.println(getObjAsString(new DataResourceBuilder().build()));

	}
	
	
	
	private String getObjAsString(Object obj) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException | ConstraintViolationException e) {
			e.printStackTrace();
		}
		return null;
	}
	private ResourceCatalog getResourceCatalog() {
		return new ResourceCatalogBuilder()
				._offeredResource_(Util.asList(new DataResourceBuilder()
						._created_(DateUtil.now())
						.build(), 
						new ImageResourceBuilder()
						._created_(DateUtil.now())
						.build(), 
						new SoftwareResourceBuilder()
						._created_(DateUtil.now())
						.build()))
				._requestedResource_(Util.asList(new AudioResourceBuilder()._created_(DateUtil.now()).build()))
				.build();
	}
	private Resource getResource() {
		return new ResourceBuilder()
				._defaultRepresentation_(Util.asList(new RepresentationBuilder()._created_(DateUtil.now()).build()))
				.build();
	}
	
	
}
