package it.eng.idsa.businesslogic.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fraunhofer.iais.eis.Message;

public class Helper {

	public static String getUUID(URI uri) {
		Pattern uuidPattern = Pattern.compile("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}");
		Matcher matcher = uuidPattern.matcher(uri.getPath());

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group(0));
		}
		return !matches.isEmpty() ? matches.get(matches.size() - 1) : null;
	}
	
	public static String getIDSMessageType(Message message) {
		return message.getClass().getSimpleName().substring(0, message.getClass().getSimpleName().lastIndexOf("Impl"));
	}

}
