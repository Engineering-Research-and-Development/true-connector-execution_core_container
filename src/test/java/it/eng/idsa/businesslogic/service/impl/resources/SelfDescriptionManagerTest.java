package it.eng.idsa.businesslogic.service.impl.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.DataResource;
import de.fraunhofer.iais.eis.ImageResource;
import de.fraunhofer.iais.eis.ImageResourceBuilder;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.resources.BadRequestException;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class SelfDescriptionManagerTest {

	private SelfDescriptionManager manager;
	private Connector conn;
	
	@BeforeEach
	public void setup() {
		manager = new SelfDescriptionManager();
		conn = SelfDescriptionUtil.getBaseConnector();
	}
	
	@Test
	public void getOfferedResource() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Resource resource = manager.getOfferedResource(conn, resourceId);
		assertNotNull(resource);
		assertTrue(resource instanceof ImageResource);
	}
	
	@Test
	public void getOfferedResource_NotFound() {
		URI resourceId = URI.create("http://resourceNotFound.com");
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.getOfferedResource(conn, resourceId));
	}
	
	@Test
	public void addOfferedResource() {
		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, conn.getResourceCatalog().get(1).getOfferedResource().size());
		Connector modifiedConnector = manager.addOfferedResource(conn, 
				URI.create("http://catalog.com/1"), 
				new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build());
		
		assertEquals(3, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void addOfferedResource_SecondResourceCatalog() {
		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, conn.getResourceCatalog().get(1).getOfferedResource().size());
		Connector modifiedConnector = manager.addOfferedResource(conn, 
				URI.create("http://catalog.com/2"), 
				new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build());
		
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(3, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void addOfferedResource_NoResourceCatalog() {
		assertThrows(ResourceNotFoundException.class,
				() ->  manager.addOfferedResource(conn, 
						URI.create("https://w3id.org/idsa/autogen/resourceCatalog/no_resource_catalog"), 
						new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build()));
	}
	
	@Test
	public void addOfferedResource_ResourceAlreadyExists() {
		String existingResourceId = "http://w3id.org/engrd/connector/artifact/catalog/1/resource/1";
		Exception ex = assertThrows(BadRequestException.class,
				() ->  manager.addOfferedResource(conn, 
						URI.create("http://catalog.com/1"), 
						new ImageResourceBuilder(URI.create(existingResourceId))
							._title_(Util.asList(new TypedLiteral("Image resource title")))
							.build()));
		assertTrue(ex.getMessage().contains(String.format("Resource with id '%s' already exists", existingResourceId)));
	}
	
	@Test
	public void updateOfferedResource() {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");

		assertTrue(conn.getResourceCatalog().get(0).getOfferedResource().get(0) instanceof DataResource);
		String TITLE_UPDATE = "Image resource title UPDATED";
		Connector modifiedConnector = manager.updateOfferedResource(conn,
				URI.create("http://catalog.com/1"), 
				new ImageResourceBuilder(artifact1URI)
					._title_(Util.asList(new TypedLiteral(TITLE_UPDATE )))
					._version_("1.0.1")
					.build());
		
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(0).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(artifact1URI))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals("1.0.1", res.get().getVersion());
		assertEquals(TITLE_UPDATE, res.get().getTitle().get(0).getValue());
		assertTrue(res.get() instanceof ImageResource);
	}
	
	@Test
	public void udpateResource_CatalogDoesNotExists() {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");
		Resource resource = new ImageResourceBuilder(artifact1URI)
				._title_(Util.asList(new TypedLiteral("Image resource title")))
				._version_("1.0.1")
				.build();
		
		assertThrows(ResourceNotFoundException.class,
				() -> manager.updateOfferedResource(conn, URI.create("https://catalogDoesNotExists"), resource));
	}
	
	@Test
	public void udpateResource_ResourceDoesNotExists() {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/1");
		Resource resource = new ImageResourceBuilder(artifact1URI)
				._title_(Util.asList(new TypedLiteral("Image resource title")))
				._version_("1.0.1")
				.build();
		
		assertThrows(ResourceNotFoundException.class,
				() -> manager.updateOfferedResource(conn, URI.create("http://catalog.com/1"), resource));
	}
	
	@Test
	public void removeOfferedResource() {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");

		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		Connector modifiedConnector = manager.deleteOfferedResource(conn, 
				artifact1URI);
		assertEquals(1, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
	}
	
	@Test
	public void getRepresentation() {
		URI representationId = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/1/representation/1");
		Representation representation = manager.getRepresentation(conn, representationId);
		assertNotNull(representation);
	}
	
	@Test
	public void getRepresentation_NotFound() {
		URI representationId = URI.create("https://representationNotFound");
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.getRepresentation(conn, representationId));
	}
	
	@Test
	public void addRepresentationToResource() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/dataRepresentation/catalog/2/resource/2");;
		Connector modifiedConnector = manager.addRepresentationToResource(conn, 
				SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
				resourceId);

		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(2, res.get().getRepresentation().size());
	}
	
	@Test
	public void addRepresentationToResource_AlreadyExists() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2/representation/1");;

		Exception ex = assertThrows(BadRequestException.class,
				() -> manager.addRepresentationToResource(conn,
						SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
						resourceId));
		assertTrue(ex.getMessage().contains(String.format("Representation with id '%s' already exists", representationURI)));
	}
	
	@Test
	public void updateRepresentationToResource() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2/representation/1");
		Connector modifiedConnector = manager.updateRepresentationToResource(conn, 
				SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
				resourceId);

		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(1, res.get().getRepresentation().size());
	}
	
	@Test
	public void updateRepresentationToResource_ResourceNotFound() {
		URI resourceId = URI.create("http://resourceNotfound");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2/representation/1");
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.updateRepresentationToResource(conn, 
				SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
				resourceId));
	}
	
	@Test
	public void addRepresentationToResource_RepresentationNotFound() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/1");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create(""), "test_file.txt");
		URI representationURI = URI.create("https://representationNotFound");
		Exception ex = assertThrows(ResourceNotFoundException.class, 
				() -> manager.updateRepresentationToResource(conn, 
						SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
						resourceId));
		assertTrue(ex.getMessage().contains("Representation with id '" + representationURI + "' does not exist"));
	}
	
	@Test
	public void removeRepresentation() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2/representation/1");

		Connector modifiedConnector = manager.removeRepresentationFromResource(conn, representationURI);

		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(0, res.get().getRepresentation().size());

	}
	
	@Test
	public void addContractOffer() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "2");
		Connector modifiedConnector = manager.addContractOfferToResource(conn, updatedOffer, resourceId);
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(2, res.get().getContractOffer().size());
	}
	
	@Test
	public void addContractOffer_AlreadyExists() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		Exception ex = assertThrows(BadRequestException.class,
				() -> manager.addContractOfferToResource(conn, updatedOffer, resourceId));
		
		assertTrue(ex.getMessage().contains("Contract offer with id '" + updatedOffer.getId() + "' already exists"));
	}
	
	@Test
	public void updateContractOffer() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/updatedOffer");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		Connector modifiedConnector = manager.updateContractOfferToResource(conn, updatedOffer, resourceId);
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(1, res.get().getContractOffer().size());
		assertEquals(targetUri, res.get().getContractOffer().get(0).getPermission().get(0).getTarget());
	}
	
	@Test
	public void updateContractOffer_ContractOfferDoesNotExists() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/updatedOffer");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "2");
		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> manager.updateContractOfferToResource(conn, updatedOffer, resourceId));
		assertTrue(ex.getMessage().contains("Contract offer with id '" + updatedOffer.getId() + "' does not exist"));
	}
	
	@Test
	public void updateContractOffer_ResourceDoesNotExists() {
		URI resourceId = URI.create("http://resourceDoesNotExists");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/updatedOffer");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> manager.updateContractOfferToResource(conn, updatedOffer, resourceId));
		assertTrue(ex.getMessage().contains("Resource with id '" + resourceId + "' not found"));
	}
	
	@Test
	public void addContractOffer_ResourceDoesNotExists() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/3/resource/1");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "2");
		
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.addContractOfferToResource(conn, updatedOffer, resourceId));
	}
	
	@Test
	public void removeContractOffer() {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		Connector modifiedConnector = manager.removeContractOfferFromResource(conn, updatedOffer.getId());
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(0, res.get().getContractOffer().size());
	}
	
	@Test
	public void getSelfDescriptionValid() {
		Connector modifiedConnector = manager.getValidConnector(conn);
		
		assertEquals(2, modifiedConnector.getResourceCatalog().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void getSelfDescriptionOneInvalid() {
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		
		Connector modified1 = manager.removeContractOfferFromResource(conn, updatedOffer.getId());
		assertEquals(2, modified1.getResourceCatalog().size());
		assertEquals(2, modified1.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modified1.getResourceCatalog().get(1).getOfferedResource().size());

		Connector modifiedConnector = manager.getValidConnector(modified1);
		assertEquals(2, modifiedConnector.getResourceCatalog().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(1, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
		
		
//		System.out.println(new Serializer().serialize(modifiedConnector));
	}
	
}
