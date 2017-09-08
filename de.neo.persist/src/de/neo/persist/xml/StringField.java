package de.neo.persist.xml;

import java.lang.reflect.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.neo.persist.annotations.Persist;

public class StringField extends PersistentField {

	public StringField(Field f, Persist p) {
		super(f, p);
	}

	@Override
	public void setValueToObject(Object object, Element element) throws IllegalAccessException {
		String attribute = element.getAttribute(mName);
		try {
			if (attribute != null)
				mField.set(object, attribute);
		} catch (NumberFormatException e) {
			// Ignore
		}
	}

	@Override
	public void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException {
		Object val = mField.get(object);
		if (val instanceof String)
			element.setAttribute(mName, (String) mField.get(object));
	}

}
