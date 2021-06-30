package it.eng.idsa.businesslogic.service.impl.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.DataResource;
import de.fraunhofer.iais.eis.ImageResource;
import de.fraunhofer.iais.eis.ImageResourceBuilder;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.resources.ResourceNotFoundException;
import it.eng.idsa.businesslogic.service.resources.SelfDescriptionManager;

public class SelfDescriptionManagerTest {

	
	private SelfDescriptionManager manager;
	private Connector conn;
	
	@BeforeEach
	public void setuop() {
		manager = new SelfDescriptionManager();
		conn = SelfDescriptionUtil.getBaseConnector();
	}
	
	@Test
	public void addOfferedResource() throws IOException {
		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, conn.getResourceCatalog().get(1).getOfferedResource().size());
		Connector modifiedConnector = manager.addOrUpdateOfferedResource(conn, 
				URI.create("http://catalog.com/1"), 
				new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build());
		
		assertEquals(3, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void addOfferedResource_SecondResourceCatalog() throws IOException {
		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, conn.getResourceCatalog().get(1).getOfferedResource().size());
		Connector modifiedConnector = manager.addOrUpdateOfferedResource(conn, 
				URI.create("http://catalog.com/2"), 
				new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build());
		
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(3, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void addOfferedResource_NoResourceCatalog() throws IOException {
		assertThrows(ResourceNotFoundException.class,
				() ->  manager.addOrUpdateOfferedResource(conn, 
						URI.create("https://w3id.org/idsa/autogen/resourceCatalog/no_resource_catalog"), 
						new ImageResourceBuilder()._title_(Util.asList(new TypedLiteral("Image resource title"))).build()));
	}
	
	@Test
	public void updateOfferedResource() throws IOException {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");

		assertTrue(conn.getResourceCatalog().get(0).getOfferedResource().get(0) instanceof DataResource);
		Connector modifiedConnector = manager.addOrUpdateOfferedResource(conn,
				URI.create("http://catalog.com/1"), 
				new ImageResourceBuilder(artifact1URI)
					._title_(Util.asList(new TypedLiteral("Image resource title")))
					._version_("1.0.1")
					.build());
		
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(0).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(artifact1URI))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals("1.0.1", res.get().getVersion());
		assertTrue(res.get() instanceof ImageResource);
	}
	
	@Test
	public void removeOfferedResource() throws JsonSyntaxException, IOException {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");

		assertEquals(2, conn.getResourceCatalog().get(0).getOfferedResource().size());
		Connector modifiedConnector = manager.deleteOfferedResource(conn, 
				artifact1URI);
		assertEquals(1, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
	}
	
	@Test
	@Disabled("Review should it throw exception or not")
	public void removeOfferedResource_NotFound() throws JsonSyntaxException, IOException {
		URI artifact1URI = URI.create("http://w3id.org/engrd/connector/artifact/catalog/1/resource/1");

		assertThrows(ResourceNotFoundException.class, 
				() -> manager.deleteOfferedResource(conn, 
						artifact1URI));
	}
	
	
	@Test
	public void addRepresentationToResource() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/dataRepresentation/catalog/2/resource/2");;
		Connector modifiedConnector = manager.addOrUpdateRepresentationToResource(conn, 
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
	public void updateRepresentationToResource() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/artifact/2"), 
				"test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2");
		Connector modifiedConnector = manager.addOrUpdateRepresentationToResource(conn, 
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
	public void addRepresentationToResource_NotFound() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/3/resource/1");
		Artifact artifact = SelfDescriptionUtil.getArtifact(URI.create(""), "test_file.txt");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/dataRepresentation/catalog/3/resource/1");
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.addOrUpdateRepresentationToResource(conn, 
						SelfDescriptionUtil.getTextRepresentation(representationURI, artifact), 
						resourceId));
	}
	
	@Test
	public void removeRepresentation() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI representationURI = URI.create("https://w3id.org/idsa/autogen/representation/catalog/2/resource/2");

		Connector modifiedConnector = manager.removeRepresentationFromResource(conn, representationURI);

		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(0, res.get().getRepresentation().size());

	}
	
	@Test
	public void addContractOffer() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "2");
		Connector modifiedConnector = manager.addOrUpdateContractOfferToResource(conn, updatedOffer, resourceId);
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(2, res.get().getContractOffer().size());
	}
	
	@Test
	public void updateContractOffer() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/updatedOffer");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		Connector modifiedConnector = manager.addOrUpdateContractOfferToResource(conn, updatedOffer, resourceId);
		
		Optional<? extends Resource> res = modifiedConnector.getResourceCatalog().get(1).getOfferedResource()
				.stream()
				.filter(resource -> resource.getId().equals(resourceId))
				.findFirst();
		assertTrue(res.isPresent());
		assertEquals(1, res.get().getContractOffer().size());
		assertEquals(targetUri, res.get().getContractOffer().get(0).getPermission().get(0).getTarget());
	}
	
	@Test
	public void addContractOffer_ResourceDoesNotExists() throws JsonSyntaxException, IOException {
		URI resourceId = URI.create("http://w3id.org/engrd/connector/artifact/catalog/3/resource/1");
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "2");
		
		assertThrows(ResourceNotFoundException.class, 
				() -> manager.addOrUpdateContractOfferToResource(conn, updatedOffer, resourceId));
	}
	
	@Test
	public void removeContractOffer() throws JsonSyntaxException, IOException {
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
		Connector modifiedConnector = manager.getConnector(conn);
		
		assertEquals(2, modifiedConnector.getResourceCatalog().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
	}
	
	@Test
	public void getSelfDescriptionOneInvalid() throws JsonSyntaxException, IOException {
		URI targetUri = URI.create("http://w3id.org/engrd/connector/artifact/catalog/2/resource/2");

		ContractOffer updatedOffer = SelfDescriptionUtil.createContractOffer(targetUri, "2", "2", "1");
		
		Connector modified1 = manager.removeContractOfferFromResource(conn, updatedOffer.getId());
		assertEquals(2, modified1.getResourceCatalog().size());
		assertEquals(2, modified1.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(2, modified1.getResourceCatalog().get(1).getOfferedResource().size());

		Connector modifiedConnector = manager.getConnector(modified1);
		assertEquals(2, modifiedConnector.getResourceCatalog().size());
		assertEquals(2, modifiedConnector.getResourceCatalog().get(0).getOfferedResource().size());
		assertEquals(1, modifiedConnector.getResourceCatalog().get(1).getOfferedResource().size());
		
		
//		System.out.println(new Serializer().serialize(modifiedConnector));
	}
}
