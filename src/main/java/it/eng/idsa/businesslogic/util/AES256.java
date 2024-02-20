package it.eng.idsa.businesslogic.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES256 {

	private static final Logger logger = LoggerFactory.getLogger(AES256.class);

	private static final int KEY_LENGTH = 256;
	private static final int ITERATION_COUNT = 65536;
	private static String secretKey;
	private static final String salt = "hd2y3vxlLv";
	private static String algorithm = "AES/GCM/NoPadding";
	
	static {
		secretKey = ObjectUtils.isNotEmpty(System.getenv("AES256-SECRET-KEY")) ? 
				System.getenv("AES256-SECRET-KEY") : "FPrnUtKJIGX1EMs";
	}
	
	public static String encrypt(String strToEncrypt) {
		try {
			SecureRandom secureRandom = new SecureRandom();
			byte[] ivBytes = new byte[16];
			secureRandom.nextBytes(ivBytes);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, ivBytes);
			
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);

			byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));
			byte[] encryptedData = new byte[ivBytes.length + cipherText.length];
			System.arraycopy(ivBytes, 0, encryptedData, 0, ivBytes.length);
			System.arraycopy(cipherText, 0, encryptedData, ivBytes.length, cipherText.length);

			return Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			logger.error("Error while encrypting", e);
			return null;
		}
	}

	public static String decrypt(String strToDecrypt) {
		try {
			byte[] encryptedData = Base64.getDecoder().decode(strToDecrypt);
			byte[] ivBytes = new byte[16];
			System.arraycopy(encryptedData, 0, ivBytes, 0, ivBytes.length);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), ITERATION_COUNT, KEY_LENGTH);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, ivBytes);

			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

			byte[] cipherText = new byte[encryptedData.length - 16];
			System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

			byte[] decryptedText = cipher.doFinal(cipherText);
			return new String(decryptedText, "UTF-8");
		} catch (Exception e) {
			logger.error("Error while decrypting", e);
			return null;
		}
	}
}
