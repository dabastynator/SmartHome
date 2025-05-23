package de.neo.persist.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.neo.persist.DaoException;
import de.neo.persist.annotations.Id;
import de.neo.persist.annotations.Persist;

public abstract class PersistentField {

	protected Field mField;
	protected String mName;
	protected XMLDaoFactory mFactory;

	public PersistentField(Field f, Persist p) {
		mField = f;
		mField.setAccessible(true);
		mName = "";
		if (p != null)
			mName = p.name();
		Id id = f.getAnnotation(Id.class);
		if (id != null)
			mName = id.name();
		if (mName.equals(""))
			mName = f.getName();
	}

	public abstract void setValueToObject(Object object, Element element) throws IllegalAccessException, DaoException,
			IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException;

	public abstract void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException, DaoException;

	public void setFactory(XMLDaoFactory factory) {
		mFactory = factory;
	}
}