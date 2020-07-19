package com.wzp.util.xml;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;

public class JaxbUtils {

	public static String marshal(Object jaxbObject) {
		StringWriter writer = new StringWriter();
		JAXB.marshal(jaxbObject, writer);
		return writer.toString();
	}
	
	public static <T> T unmarshal(String xml, Class<T> clazz) {
		return JAXB.unmarshal(new StringReader(xml), clazz);
	}
}
