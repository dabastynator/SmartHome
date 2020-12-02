package de.neo.persist.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.DaoFactory;

public class XMLDaoFactory extends DaoFactory {

	private File mXmlFile;
	private boolean mFlushOnChange;
	protected Map<Class<?>, XMLDao<?>> mXmlMapClassDao = new HashMap<>();
	protected List<XMLDao<?>> mXmlDaoList = new ArrayList<>();
	private String mRootName = "Root";

	public String getRootName() {
		return mRootName;
	}

	public void setRootName(String rootName) {
		mRootName = rootName;
	}

	protected XMLDaoFactory(File xmlFile) {
		super();
		mXmlFile = xmlFile;
	}

	public void flushOnChange(boolean flushOnChange) {
		mFlushOnChange = flushOnChange;
	}

	public void flush() throws DaoException {
		flush(mXmlFile);
	}

	public synchronized void flush(File xmlFile) throws DaoException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement(mRootName);

			for (XMLDao<?> dao : mXmlDaoList)
				dao.initWriteXML();

			doc.appendChild(root);
			for (XMLDao<?> dao : mXmlDaoList)
				dao.writeXML(doc, root);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Result output = new StreamResult(mXmlFile);
			Source input = new DOMSource(doc);

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(input, output);

		} catch (ParserConfigurationException | IllegalArgumentException | IllegalAccessException
				| TransformerFactoryConfigurationError | TransformerException e) {
			throw new DaoException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	public void read() throws DaoException {
		read(mXmlFile);
	}

	public void read(File xmlFile) throws DaoException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			for (XMLDao<?> dao : mXmlDaoList)
				dao.initReadXML(doc);

			for (XMLDao<?> dao : mXmlDaoList)
				dao.readXML(doc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new DaoException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	public void notifyChange() throws DaoException {
		if (mFlushOnChange)
			flush();
	}

	public static class XMLFactoryBuilder extends FactoryBuilder {

		private File mXmlFile;
		private boolean mFlushOnChange = false;

		public File getXmlFile() {
			return mXmlFile;
		}

		public XMLFactoryBuilder setXmlFile(File xmlFile) {
			mXmlFile = xmlFile;
			return this;
		}

		@Override
		public DaoFactory createDaoFactory() throws DaoException {
			if (mXmlFile == null)
				throw new DaoException("XML File missing for xml dao factory");
			XMLDaoFactory factory = new XMLDaoFactory(mXmlFile);
			for (Dao<?> dao : mDaoList) {
				XMLDao<?> xmlDao = (XMLDao<?>) dao;
				factory.mXmlDaoList.add(xmlDao);
				factory.mXmlMapClassDao.put(xmlDao.getDomainClass(), xmlDao);
				factory.mDaoList.add(xmlDao);
				factory.mMapClassDao.put(xmlDao.getDomainClass(), xmlDao);
				xmlDao.setFactory(factory);
			}
			factory.mFlushOnChange = mFlushOnChange;
			if (mXmlFile.exists())
				factory.read();
			return factory;
		}

		public XMLFactoryBuilder setFlushOnChange(boolean b) {
			mFlushOnChange = b;
			return this;
		}

	}

}
