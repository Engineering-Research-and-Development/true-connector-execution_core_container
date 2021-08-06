package it.eng.idsa.businesslogic.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class JwTokenUtil {
	
	public static String generateToken(boolean expired) {
		String id = "";
		String issuer = "demo_token";
		String subject = "demo token subject";
		Date currentDate = new Date();

		Date exp = isExpired(expired);
		
		// Let's set the JWT Claims
		String token = JWT.create()
				.withJWTId(id)
				.withIssuedAt(currentDate)
				.withSubject(subject)
				.withIssuer(issuer)
				.withExpiresAt(exp)
				.sign(Algorithm.none());

		return token;
	}

	public static Date isExpired(boolean expired) {
		Date currentDate = new Date();
		LocalDateTime localDateTime = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if(!expired) {
			localDateTime = localDateTime.plusMinutes(30);
		} else {
			localDateTime = localDateTime.minusMinutes(30);
		}
		
		Date exp = new Date(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime());
		return exp;
	}
}