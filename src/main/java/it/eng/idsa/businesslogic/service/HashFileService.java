package it.eng.idsa.businesslogic.service;

/**
 * @author Antonio Scatoloni on 22/04/2020
 **/

public interface HashFileService {

    String hash(String payload);
//    void recordHash(String hash, String payload);
    String getContent(String hash) throws Exception;
}
