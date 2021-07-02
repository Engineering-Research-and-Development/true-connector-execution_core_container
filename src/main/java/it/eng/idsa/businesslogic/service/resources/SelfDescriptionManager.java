package it.eng.idsa.businesslogic.service.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.JsonLDModule;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.businesslogic.configuration.SelfDescriptionConfiguration;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class SelfDescriptionManager {
	
	protected static final String ID = "@id";
	protected static final String RESOURCE_CATALOG = "ids:resourceCatalog";
	protected static final String OFFERED_RESOURCE = "ids:offeredResource";
	protected static final String REPRESENTATION = "ids:representation";
	protected static final String CONTRACT_OFFER = "ids:contractOffer";
	
	private static final Logger logger = LoggerFactory.getLogger(SelfDescriptionManager.class);

	private static ObjectMapper mapper = new ObjectMapper();
	private static Gson gson = new Gson();
	private Serializer serializer = new Serializer();
	
	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	static {
		mapper.registerModule(new JsonLDModule());
	}
	
	/**
	 * Adds resource to Offered resources in Resource Catalog with resourceCatalogId if Resource does not exists</br>
	 * Otherwise updates existing resource (remove it and add as whole)
	 * @param connector
	 * @param resourceCatalogId
	 * @param resource
	 * @return new connector instance
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public Connector addOrUpdateOfferedResource(Connector connector, URI resourceCatalogId, Resource resource) throws JsonSyntaxException, IOException {
		JsonElement jsonElement = gson.fromJson(mapper.writeValueAsString(connector), JsonElement.class);
		JsonArray resourceCatalogJsonArray = (JsonArray) jsonElement.getAsJsonObject().get(RESOURCE_CATALOG);
		
		Optional<JsonElement> opt = StreamSupport.stream(resourceCatalogJsonArray.spliterator(), false)
			.filter(p -> p.getAsJsonObject().get(ID).getAsString().equals(resourceCatalogId.toString()))
			.findFirst();

		if(opt.isPresent()) {
			JsonElement resourceCatalog = opt.get();
			if(resourceCatalog.getAsJsonObject().get(ID).getAsString().equals(resourceCatalogId.toString())) {
				logger.debug("Found resourceCatalog with id {}", resourceCatalogId);
				
				JsonArray offeredResources = resourceCatalog.getAsJsonObject().get(OFFERED_RESOURCE).getAsJsonArray();
				
				// find if such resource exists so we can remove it and add in next step
				removeResource(resource.getId().toString(), resourceCatalog);
				
				JsonObject add = null;
				try {
					add = gson.fromJson(serializer.serialize(resource), JsonObject.class);
				} catch (JsonSyntaxException | IOException e) {
					logger.error("Error while converting resource to json object", e);
				}
				offeredResources.add(gson.toJsonTree(add));
			} else {
				logger.info("Did not find resourceCatalog with id {}", resourceCatalogId);
				throw new ResourceNotFoundException("Did not find resourceCatalog with id " + resourceCatalogId);
			}
		} else {
			logger.info("Did not find resourceCatalog with id {}", resourceCatalogId);
			throw new ResourceNotFoundException("Did not find resourceCatalog with id " + resourceCatalogId);
		}
//		SelfDescription.getInstance().setBaseConnector(serializer.deserialize(gson.toJson(jsonElement), Connector.class));
//		return SelfDescription.getInstance().getConnector();
		return serializer.deserialize(gson.toJson(jsonElement), Connector.class);
	}
	
	/**
	 * Remove resource from any Resource Catalogs
	 * @param connector
	 * @param resourceId
	 * @return new connector instance
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public Connector deleteOfferedResource(Connector connector, URI resourceId) 
			throws JsonSyntaxException, IOException {
		BaseConnectorImpl connImpl = (BaseConnectorImpl) connector;
		boolean removed = false;
		for(ResourceCatalog resourceCatalog : connImpl.getResourceCatalog()) {
			removed = resourceCatalog.getOfferedResource().removeIf(or -> or.getId().equals(resourceId));
		}
		if(removed) {
			logger.info("Succesfuly removed resource with id '{}'", resourceId);
		} else {
			 logger.info("Did not find resource with id '{}'", resourceId);
		}
		return connector;
	}
	
	/**
	 * Find offered resource from all catalogs
	 * @param connector
	 * @param resourceId
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public Resource getOfferedResource(Connector connector, URI resourceId) 
			throws JsonSyntaxException, IOException {
		
		ListIterator<? extends ResourceCatalog> litr = null;
		litr = connector.getResourceCatalog().listIterator();
		while(litr.hasNext()) {
			ResourceCatalog rc = litr.next();
			ListIterator<? extends Resource> resourceIter = rc.getOfferedResource().listIterator();
			while(resourceIter.hasNext()) {
				Resource resource = resourceIter.next();
				if(resource.getId().equals(resourceId)) {
					return (Resource) resource;
				}
			}
		}
		throw new ResourceNotFoundException("Resource with id '" + resourceId + "' not found");
	}
	
	public Representation getRepresentation(URI representationId) {
		BaseConnectorImpl connector = (BaseConnectorImpl) SelfDescription.getInstance().getConnector();
		
		for(ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for(Resource resource : resourceCatalog.getOfferedResource()) {
				for(Representation representation : resource.getRepresentation()) {
					if(representation.getId().equals(representationId)) {
						logger.debug("Found representation with id '{}'", representationId);
						return representation;
					}
				}
			}
		}
		throw new ResourceNotFoundException(String.format("Did not find representation with id '%s'", representationId));
	}
	
	/**
	 * Add or update representation for resource
	 * @param connector
	 * @param representation
	 * @param resourceId
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public Connector addOrUpdateRepresentationToResource(Connector connector, Representation representation, URI resourceId) 
			throws JsonSyntaxException, IOException {
		JsonElement jsonElement = gson.fromJson(mapper.writeValueAsString(connector), JsonElement.class);
		JsonArray resourceCatalogJsonArray = (JsonArray) jsonElement.getAsJsonObject().get(RESOURCE_CATALOG);
		
		JsonElement[] resource = findOfferedResource(resourceId, resourceCatalogJsonArray);
		if(resource[0] != null) {
			logger.info("Found resource to add new representation");
			JsonObject add = null;
			try {
				add = gson.fromJson(serializer.serialize(representation), JsonObject.class);
			} catch (JsonSyntaxException | IOException e) {
				logger.error("Error while converting resource to json object", e);
			}
			// TODO - find if such representation exits or not
			Optional<JsonElement> opt = StreamSupport.stream(resource[0].getAsJsonObject().get(REPRESENTATION).getAsJsonArray().spliterator(), false)
					.filter(p -> p.getAsJsonObject().get(ID).getAsString().equals(representation.getId().toString()))
					.findFirst();
			if(opt.isPresent()) {
				logger.info("Removing existing representation before updating it");
				removeRepresentation(representation.getId().toString(), 
						resource[0].getAsJsonObject().get(REPRESENTATION).getAsJsonArray());
			}
			resource[0].getAsJsonObject().get(REPRESENTATION).getAsJsonArray().add(gson.toJsonTree(add));
			
		} else {
			String message = String.format("Resource with id '%s' to add new representation not found", resourceId);
			logger.info(message);
			throw new ResourceNotFoundException(message);
		}
		return serializer.deserialize(gson.toJson(jsonElement), Connector.class);
	}
	
	/**
	 * Remove representation from resource
	 * @param connector
	 * @param representationId
	 * @return
	 */
	public Connector removeRepresentationFromResource(Connector connector, URI representationId) {
		
		for(ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for(Resource resource : resourceCatalog.getOfferedResource()) {
				resource.getRepresentation().removeIf(rep -> rep.getId().equals(representationId));
			}
		}
		return connector;
	}

	public ContractOffer getContractOffer(URI contractOfferId) {
		BaseConnectorImpl connector = (BaseConnectorImpl) SelfDescription.getInstance().getConnector();
		
		for(ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for(Resource resource : resourceCatalog.getOfferedResource()) {
				for(ContractOffer co : resource.getContractOffer()) {
					if(co.getId().equals(contractOfferId)) {
						logger.debug("Found contract offer with id '{}'", contractOfferId);
						return co;
					}
				}
			}
		}
		throw new ResourceNotFoundException(String.format("Did not find contract offer with id '%s'", contractOfferId));
	}
	
	/**
	 * Add or update contract offer to resource
	 * @param connector
	 * @param contractOffer
	 * @param resourceId
	 * @return
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	public Connector addOrUpdateContractOfferToResource(Connector connector, ContractOffer contractOffer, URI resourceId) 
			throws JsonSyntaxException, IOException {
		JsonElement jsonElement = gson.fromJson(mapper.writeValueAsString(connector), JsonElement.class);
		JsonArray resourceCatalogJsonArray = (JsonArray) jsonElement.getAsJsonObject().get(RESOURCE_CATALOG);
		
		JsonElement[] resource = findOfferedResource(resourceId, resourceCatalogJsonArray);
		
		if(resource[0] != null) {
			logger.info("Found resource to add or update contract offer");
			// TODO - find if such representation exits or not
			Optional<JsonElement> opt = StreamSupport.stream(resource[0].getAsJsonObject().get(CONTRACT_OFFER).getAsJsonArray().spliterator(), false)
					.filter(p -> p.getAsJsonObject().get(ID).getAsString().equals(contractOffer.getId().toString()))
					.findFirst();
			if(opt.isPresent()) {
				logger.debug("About to remove existing contract offer");
				removeRepresentation(contractOffer.getId().toString(), 
						resource[0].getAsJsonObject().get(CONTRACT_OFFER).getAsJsonArray());
			} 
			JsonObject add = null;
			try {
				add = gson.fromJson(serializer.serialize(contractOffer), JsonObject.class);
			} catch (JsonSyntaxException | IOException e) {
				logger.error("Error while converting contract offer to json object", e);
			}
			resource[0].getAsJsonObject().get(CONTRACT_OFFER).getAsJsonArray().add(gson.toJsonTree(add));
		} else {
			String message = String.format("Resource with id '%s' to remove contract offer '%s' not found", 
					resourceId, contractOffer.getId());
			logger.info(message);
			throw new ResourceNotFoundException(message);
		}
		return serializer.deserialize(gson.toJson(jsonElement), Connector.class);
	}
	
	/**
	 * Remove contract offer from resource
	 * @param connector
	 * @param contractOfferId
	 * @return
	 */
	public Connector removeContractOfferFromResource(Connector connector, URI contractOfferId)  {
		for(ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for(Resource resource : resourceCatalog.getOfferedResource()) {
				resource.getContractOffer().removeIf(co -> co.getId().equals(contractOfferId));
			}
		}
		return connector;
	}
	
	/**
	 * Return only offered resources that have: 
	 * </br>at least one representation with at least one artifact and </br>at least one contract offer
	 * @param connector
	 * @return
	 */
	public Connector getValidConnector(Connector connector) {
		ListIterator<? extends ResourceCatalog> litr = null;
		litr = connector.getResourceCatalog().listIterator();
		while(litr.hasNext()) {
			ResourceCatalog rc = litr.next();
			if(rc.getOfferedResource() != null) {
				ListIterator<? extends Resource> resourceIter = rc.getOfferedResource().listIterator();
				while(resourceIter.hasNext()) {
					Resource r = resourceIter.next();
					boolean emptyContractOffer = r.getContractOffer() !=null ? r.getContractOffer().size() == 0 : true;
					boolean validRepresentation = false;
					if(r.getRepresentation() != null) {
						ListIterator<? extends Representation> representationIter = r.getRepresentation().listIterator();
						while(representationIter.hasNext()) {
							Representation rep = representationIter.next();
							validRepresentation = rep.getInstance().size() != 0;
						}
					}
					if(!emptyContractOffer && validRepresentation) {
						logger.debug("Valid resource");
					} else {
						resourceIter.remove();
					}
				}
			} else {
				logger.info("Empty resource catalog - returning self description document as is");
			}
		}
		return connector;
	}

	public void saveConnector() {
		logger.info("Persisting connector to file storage");
		try {
			String connectorAsString = MultipartMessageProcessor.serializeToJsonLD(SelfDescription.getInstance().getConnector());
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(
						selfDescriptionConfiguration.getFileLocation()
						+ File.separator 
						+ SelfDescriptionConfiguration.SELF_DECRIPTION_FILE_NAME);
				fos.write(connectorAsString.getBytes());
			} finally {
				if(fos != null) {
					fos.close();
				}
			}
		} catch (IOException e) {
			logger.error("Error while trying to save connector to filesystem", e);
		}
	}
	
	public Connector loadConnector() {
		logger.debug("File location : ", selfDescriptionConfiguration.getFileLocation());
		File selfDescriptionFile = new File(selfDescriptionConfiguration.getFileLocation()
				 + File.separator + SelfDescriptionConfiguration.SELF_DECRIPTION_FILE_NAME);
		Connector connector = null;
		if(selfDescriptionFile.exists() && !selfDescriptionFile.isDirectory()) { 
		    logger.info("Found existing self description document at {}", selfDescriptionFile.getAbsoluteFile());
		    // if exists - load it
		    String content;
			try {
				logger.debug("Reading connector from file...");
				content = Files.readString(selfDescriptionFile.toPath(), StandardCharsets.UTF_8);
				logger.debug("Deserializing from file...");
				connector = new Serializer().deserialize(content, Connector.class);
				logger.debug("Setting loaded connector...");
				SelfDescription.getInstance().setBaseConnector(connector);
				logger.debug("Done with loading connector from file.");
			} catch (IOException e) {
				logger.error("Error while loading connector from file '{}'", selfDescriptionFile.getAbsoluteFile());
			}
		}
		return connector;
	}
	
	private void removeResource(String resourceId, JsonElement resourceCatalog) {
		JsonArray offeredResources = resourceCatalog.getAsJsonObject().get(OFFERED_RESOURCE).getAsJsonArray();
		Iterator<JsonElement> it = offeredResources.iterator();
		while (it.hasNext()) {
			JsonElement jsonObject = it.next();
		    if (jsonObject.getAsJsonObject().get(ID).getAsString().equals(resourceId)) {
		    	logger.debug("Removing resource with id {}", resourceId);
		        it.remove();
		    }
		}
	}
	
	private void removeRepresentation(String id, JsonArray resourceCatalog) {
		Iterator<JsonElement> it = resourceCatalog.iterator();
		while (it.hasNext()) {
			JsonElement jsonObject = it.next();
		    if (jsonObject.getAsJsonObject().get(ID).getAsString().equals(id)) {
		    	logger.debug("Removing object with id {}", id);
		        it.remove();
		    }
		}
	}
	
	// TODO find better solution not to use wrapper/array to get resource
	private JsonElement[] findOfferedResource(URI resourceId, JsonArray resourceCatalogJsonArray) {
		JsonElement[] resource = new JsonElement[] {null};
		resourceCatalogJsonArray.forEach(rc -> {
			rc.getAsJsonObject().get(OFFERED_RESOURCE).getAsJsonArray().forEach(or -> {
				if(or.getAsJsonObject().get(ID).getAsString().equals(resourceId.toString())) {
					resource[0] = or;
				} 
			});
		});
		return resource;
	}

}
