package it.eng.idsa.businesslogic.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

public class JwTokenUtil {
	
	public static String generateToken(boolean expired) {
		String id = "";
		String issuer = "demo_token";
		String subject = "demo token subject";
		Date currentDate = new Date();

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder()
				.setId(id)
				.setIssuedAt(currentDate)
				.setSubject(subject)
				.setIssuer(issuer);
		
		LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if(!expired) {
			localDateTime = localDateTime.plusMinutes(30);
		} else {
			localDateTime = localDateTime.minusMinutes(30);
		}
		
		Date exp = new Date(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime());
		builder.setExpiration(exp);

		return builder.compact();
	}
}
