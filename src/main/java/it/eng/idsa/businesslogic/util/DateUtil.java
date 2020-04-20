package it.eng.idsa.businesslogic.util;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateUtil {
	
	private static DatatypeFactory datatypeFactory = null;
	
	static {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("Error while trying to obtain a new instance of DatatypeFactory", e);
		} 
	}
	
	public static XMLGregorianCalendar now() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis((new Date()).getTime());
		return datatypeFactory.newXMLGregorianCalendar(gc);
	}
	
}
