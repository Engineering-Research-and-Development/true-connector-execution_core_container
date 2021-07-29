package it.eng.idsa.businesslogic.service.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
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
	
	@Autowired
	private SelfDescriptionConfiguration selfDescriptionConfiguration;
	
	/**
	 * Offered Resource
	 */
	
	/**
	 * Adds resource to Offered resources in Resource Catalog with resourceCatalogId if Resource does not exists</br>
	 * Otherwise throws BadRequestException
	 * @param connector
	 * @param resourceCatalogId
	 * @param resource
	 * @return new connector instance
	 */
	@SuppressWarnings("unchecked")
	public Connector addOfferedResource(Connector connector, URI resourceCatalogId, Resource resource) {
		ResourceCatalog resourceCatalog = checkIfResourceCatalogExists(connector, resourceCatalogId);

		boolean existingOfferedResource = resourceCatalog.getOfferedResource().stream()
				.anyMatch(r -> r.getId().equals(resource.getId()));
				
		if(!existingOfferedResource) {
			logger.debug("Resource with id '{}' does not exists in catalog - proceeding with adding it", resource.getId());
			logger.info("Adding resource to catalog");
			((List<Resource>) resourceCatalog.getOfferedResource()).add(resource);
		} else {
			logger.info("Resource with id '{}' already exists", resource.getId());
			throw new BadRequestException(String.format("Resource with id '%s' already exists",  resource.getId()));
		}
		return connector;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Update resource for giver resource catalog
	 * @param connector
	 * @param resourceCatalogId
	 * @param resource
	 * @return updated connector
	 */
	public Connector updateOfferedResource(Connector connector, URI resourceCatalogId, Resource resource) {
		ResourceCatalog resourceCatalog = checkIfResourceCatalogExists(connector, resourceCatalogId);

		Predicate<Resource> equalResource = r -> r.getId().equals(resource.getId());
		boolean existingOfferedResource = resourceCatalog.getOfferedResource().stream()
				.anyMatch(equalResource);
		
		if(existingOfferedResource) {
			logger.debug("Found resource with id '{}' for update/replace", resource.getId());
			resourceCatalog.getOfferedResource().removeIf(equalResource);
			logger.info("Updating resource");
			((ArrayList<Resource>) resourceCatalog.getOfferedResource()).add(resource);
		} else {
			logger.info("Resource with id '{}' does not exist, cannot update", resource.getId());
			throw new ResourceNotFoundException(String.format("Resource with id '%s' does not exist", resource.getId()));
		}
		return connector;
	}

	/**
	 * Remove resource from any Resource Catalogs
	 * @param connector
	 * @param resourceId
	 * @return new connector instance
	 */
	public Connector deleteOfferedResource(Connector connector, URI resourceId) {
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
	 */
	public Resource getOfferedResource(Connector connector, URI resourceId) {
		
		Optional<Resource> resource = Optional.ofNullable(connector.getResourceCatalog().stream()
			.flatMap(rc -> rc.getOfferedResource().stream())
			.filter(or -> or.getId().equals(resourceId))
			.findFirst()
			.orElse(null));
		
		if(resource.isPresent()) {
			return resource.get();
		}
		throw new ResourceNotFoundException("Resource with id '" + resourceId + "' not found");
	}
	
	private ResourceCatalog checkIfResourceCatalogExists(Connector connector, URI resourceCatalogId) {
		ResourceCatalog resourceCatalog = connector.getResourceCatalog().stream()
			.filter(rc -> rc.getId().equals(resourceCatalogId))
			.findAny()
			.orElse(null);
		if(resourceCatalog == null) {
			logger.info("Did not find resourceCatalog with id {}", resourceCatalogId);
			throw new ResourceNotFoundException("Did not find resourceCatalog with id " + resourceCatalogId);
		}
		logger.debug("Found resource catalog with Id to add new resource");
		return resourceCatalog;
	}
	
	/**
	 * Representation
	 */
	
	/**
	 * Return representation with provided id
	 * @param representationId
	 * @return
	 */
	public Representation getRepresentation(Connector connector, URI representationId) {
		Optional<Representation> representation = Optional.ofNullable(connector.getResourceCatalog().stream()
				.flatMap(rc -> rc.getOfferedResource().stream())
				.flatMap(or -> or.getRepresentation().stream())
				.filter(r -> r.getId().equals(representationId))
				.findFirst()
				.orElse(null));
		if(representation.isPresent()) {
			return representation.get();
		}
		throw new ResourceNotFoundException(String.format("Did not find representation with id '%s'", representationId));
	}
	
	/**
	 * Add or update representation for resource
	 * @param connector
	 * @param representation
	 * @param resourceId
	 * @return
	 */
	public Connector addRepresentationToResource(Connector connector, Representation representation, URI resourceId) {
		Resource resource = getOfferedResource(connector, resourceId);
		
		Predicate<Representation> equalRepresentation = r -> r.getId().equals(representation.getId());
		boolean representationExists = resource.getRepresentation().stream()
				.anyMatch(equalRepresentation);	
		
		if(!representationExists) {
			logger.info("Addind representation with id '{}' to resource '{}'", representation.getId(), resourceId);
			((List<Representation>) resource.getRepresentation()).add(representation);
		} else {
			logger.info("Representation with id '{}' already exists", representation.getId());
			throw new BadRequestException(String.format("Representation with id '%s' already exists",  representation.getId()));
		}
		return connector;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Update representation for given resource
	 * @param connector
	 * @param representation
	 * @param resourceId
	 * @return
	 */
	public Connector updateRepresentationToResource(Connector connector, Representation representation, URI resourceId) {
		Resource resource = getOfferedResource(connector, resourceId);
		
		Predicate<Representation> equalRepresentation = r -> r.getId().equals(representation.getId());
		boolean representationExists = resource.getRepresentation().stream()
				.anyMatch(equalRepresentation);
		if(representationExists) {
			logger.debug("Found existing representation for update");
			resource.getRepresentation().removeIf(equalRepresentation);
			logger.info("Updating representation");
			((List<Representation>) resource.getRepresentation()).add(representation);
		} else {
			logger.info("Representation with id '{}' does not exist, cannot update", representation.getId());
			throw new ResourceNotFoundException(String.format("Representation with id '%s' does not exist", representation.getId()));
		}
		return connector;
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

	
	/**
	 * Contract offer
	 */
	
	/**
	 * 
	 * @param contractOfferId
	 * @return
	 */
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
	public Connector addContractOfferToResource(Connector connector, ContractOffer contractOffer, URI resourceId)  {
		Resource resource = getOfferedResource(connector, resourceId);

		Predicate<ContractOffer> equalContractOffer = co -> co.getId().equals(contractOffer.getId());
		boolean contractOfferExists = resource.getContractOffer().stream()
				.anyMatch(equalContractOffer);	
		
		if(!contractOfferExists) {
			logger.info("Addind contract offer with id '{}' to resource '{}'", contractOffer.getId(), resourceId);
			((List<ContractOffer>) resource.getContractOffer()).add(contractOffer);
		} else {
			logger.info("Contract offer with id '{}' already exists", contractOffer.getId());
			throw new BadRequestException(String.format("Contract offer with id '%s' already exists",  contractOffer.getId()));
		}
		return connector;
		
	}
	
	/**
	 * Updating contract offer for representation 
	 * @param connector
	 * @param contractOffer
	 * @param resourceId
	 * @return
	 */
	public Connector updateContractOfferToResource(Connector connector, ContractOffer contractOffer, URI resourceId)  {
		Resource resource = getOfferedResource(connector, resourceId);
		
		Predicate<ContractOffer> equalContractOffer = co -> co.getId().equals(contractOffer.getId());
		boolean contractOfferExists = resource.getContractOffer().stream()
				.anyMatch(equalContractOffer);	
		
		if(contractOfferExists) {
			logger.debug("Found existing contract offer for update");
			resource.getContractOffer().removeIf(equalContractOffer);
			logger.info("Updating contract offer");
			((List<ContractOffer>) resource.getContractOffer()).add(contractOffer);
		} else {
			logger.info("Contract offer with id '{}' does not exist, cannot update", contractOffer.getId());
			throw new ResourceNotFoundException(String.format("Contract offer with id '%s' does not exist", contractOffer.getId()));
		}
		return connector;
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
				logger.debug(e.getMessage());
			}
		}
		return connector;
	}

}
