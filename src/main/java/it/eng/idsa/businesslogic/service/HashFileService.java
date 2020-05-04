package it.eng.idsa.businesslogic.service;

import it.eng.idsa.clearinghouse.model.NotificationContent;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

public interface HashFileService {

    String hash(String payload);
    void recordHash(String hash, String payload, NotificationContent notificationContent);
    String getContent(String hash) throws Exception;
}
