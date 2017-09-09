package de.neo.persist.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.annotations.Persist;

public class DomainField extends PersistentField {

	public DomainField(Field f, Persist p) {
		super(f, p);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setValueToObject(Object object, Element element) throws IllegalAccessException, DaoException,
			IllegalArgumentException, InstantiationException, InvocationTargetException {
		Dao dao = mFactory.getDao(mField.getType());
		if (element.hasAttribute(mName)) {
			try {
				Long id = Long.valueOf(element.getAttribute(mName));
				Object value = dao.loadById(id);
				mField.set(object, value);
			} catch (NumberFormatException e) {
				// ignore
			}
		} else if (dao instanceof XMLDao) {
			XMLDao xmlDao = (XMLDao) dao;
			NodeList nodes = element.getElementsByTagName(mName);
			if (nodes.getLength() > 0 && nodes.item(0) instanceof Element) {
				Element e = (Element) nodes.item(0);
				Object value = xmlDao.readDomainXML(e);
				mField.set(object, value);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException, DaoException {
		Object value = mField.get(object);
		if (value != null) {
			Dao dao = mFactory.getDao(mField.getType());
			if (!(dao instanceof XMLDao))
				throw new DaoException("Domain references must be between same dao implementations.");
			XMLDao xmlDao = ((XMLDao) dao);
			if (xmlDao.alreadySaved(value)) {
				Long id = xmlDao.getId(value);
				element.setAttribute(mName, String.valueOf(id));
			} else {
				Element e = xmlDao.writeDomainXML(doc, element, value);
				doc.renameNode(e, null, mName);
			}
		}
	}

}
