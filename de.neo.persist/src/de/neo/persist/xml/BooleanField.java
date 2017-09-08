package de.neo.persist.xml;

import java.lang.reflect.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.neo.persist.annotations.Persist;

public class BooleanField extends PersistentField {

	public BooleanField(Field f, Persist p) {
		super(f, p);
	}

	@Override
	public void setValueToObject(Object object, Element element) throws IllegalAccessException {
		String attribute = element.getAttribute(mName);
		if (attribute != null)
			mField.setBoolean(object, Boolean.valueOf(attribute));
	}

	@Override
	public void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException {
		element.setAttribute(mName, String.valueOf(mField.getBoolean(object)));

	}

}
