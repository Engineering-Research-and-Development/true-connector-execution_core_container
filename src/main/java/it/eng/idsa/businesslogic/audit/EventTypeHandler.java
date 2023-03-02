package it.eng.idsa.businesslogic.audit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class EventTypeHandler {
	
	//from property file
	private List<String> selectedEvents;
	
	//final config
	private Set<TrueConnectorEventType> configuredEvents = new HashSet<>();

    public EventTypeHandler(@Value("#{'${application.logging.auditableEvents:ALL}'.split(',')}") List<String> selectedEvents) {
		this.selectedEvents = selectedEvents;
	}
	
	@PostConstruct
	public void configure () {
		//add no event auditing
		if (selectedEvents.contains("NONE")) {
			return;
		}
		
		//audit log every event
		if (selectedEvents.contains("ALL")) {
			for (TrueConnectorEventType e : TrueConnectorEventType.values()) {
				configuredEvents.add(e);
			}
			return;
		}
		
		if (selectedEvents.contains("SELF_DESCRIPTION_ALL")) {
			selectedEvents.add("SELF_DESCRIPTION");
			selectedEvents.add("CONTRACT_OFFER");
			selectedEvents.add("OFFERED_RESOURCE");
			selectedEvents.add("REPRESENTATION");
		}
		
		
		for (TrueConnectorEventType event : TrueConnectorEventType.values()) {
			for (String conf : selectedEvents) {
				if (event.name().contains(conf)) {
					configuredEvents.add(event);
				}
			}
		}
		
		configuredEvents.add(TrueConnectorEventType.TRUE_CONNECTOR_EVENT);
		configuredEvents.add(TrueConnectorEventType.HTTP_REQUEST_RECEIVED);
	}
	
	/**
	 * Returns true if event should be logged.
	 * 
	 * @param eventTypes array of event types
	 * @return boolean contains or not event
	 */
	public boolean shouldAuditEvent(TrueConnectorEventType... eventTypes) {
		return CollectionUtils.containsAny(configuredEvents, CollectionUtils.arrayToList(eventTypes));
	}
}
