package de.neo.persist.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.neo.persist.annotations.Persist;

public class EnumField extends PersistentField {

	private Enum[] mEnumList;

	public EnumField(Field f, Persist p) {
		super(f, p);

		try {
			Method method = f.getType().getDeclaredMethod("values");
			Object obj = method.invoke(null);
			mEnumList = (Enum[]) obj;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void setValueToObject(Object object, Element element) throws IllegalAccessException {
		String attribute = element.getAttribute(mName);
		try {
			for (Enum e : mEnumList) {
				if (e.toString().equals(attribute)) {
					mField.set(object, e);
				}
			}
		} catch (NumberFormatException e) {
			// Ignore
		}
	}

	@Override
	public void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException {
		Enum value = (Enum) mField.get(object);
		if (value != null) {
			element.setAttribute(mName, value.toString());
		}

	}

}
