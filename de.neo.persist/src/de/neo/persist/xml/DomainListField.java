package de.neo.persist.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;

public class DomainListField extends PersistentField {

	private DaoFactory mFactory;
	private OneToMany mOneToMany;

	public DomainListField(Field f, Persist p, XMLDaoFactory factory, OneToMany oneToMany) {
		super(f, p);
		mFactory = factory;
		mOneToMany = oneToMany;
		if (!oneToMany.name().equals(""))
			mName = oneToMany.name();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValueToObject(Object object, Element element)
			throws IllegalAccessException, DaoException, IllegalArgumentException, InstantiationException,
			InvocationTargetException {
		Dao dao = mFactory.getDao(mOneToMany.domainClass());
		Object field = mField.get(object);
		if ((field instanceof List) && (dao instanceof XMLDao)) {
			List list = (List) field;
			XMLDao xmlDao = (XMLDao) dao;
			NodeList nodes = element.getElementsByTagName(mName);
			for (int i = 0; i < nodes.getLength(); i++) {
				Element e = (Element) nodes.item(i);
				Object value = xmlDao.readDomainXML(e);
				list.add(value);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setValueToXML(Document doc, Object object, Element element)
			throws IllegalArgumentException, IllegalAccessException, DaoException {
		Object field = mField.get(object);
		if (field != null && field instanceof List) {
			Dao dao = mFactory.getDao(mOneToMany.domainClass());
			List list = (List) field;
			if (!(dao instanceof XMLDao))
				throw new DaoException("Domain references must be between same dao implementations.");
			XMLDao xmlDao = ((XMLDao) dao);
			for (Object value : list) {
				if (xmlDao.alreadySaved(value)) {
					Element e = doc.createElement(mName);
					e.setAttribute("id", String.valueOf(xmlDao.getId(value)));
				} else {
					Element e = xmlDao.writeDomainXML(doc, element, value);
					doc.renameNode(e, null, mName);
				}
			}
		}
	}

}
