package it.eng.idsa.businesslogic.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AES256Test {

	private String originalString = "String used to test AES256 encryption/decryption";

	@Test
	public void encryptDecrypt() {
		String encrypted = AES256.encrypt(originalString);
		assertNotNull(encrypted);
		System.out.println(encrypted);
		String decrypted = AES256.decrypt(encrypted);
		assertEquals(originalString, decrypted);
		System.out.println(decrypted);
	}	
}
