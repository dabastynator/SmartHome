package de.neo.persist.xml;

import java.io.File;
import java.io.IOException;

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

	public static DaoFactory initiate(File xmlFile) {
		mSingelton = new XMLDaoFactory(xmlFile);
		return mSingelton;
	}

	private File mXmlFile;
	private boolean mFlushOnChange;
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

	public static void flushToFile() throws DaoException {
		if (!(mSingelton instanceof XMLDaoFactory))
			throw new DaoException("DaoFactory must be type of XML");
		((XMLDaoFactory) mSingelton).flush();
	}

	public static void readFromFile() throws DaoException {
		if (!(mSingelton instanceof XMLDaoFactory))
			throw new DaoException("DaoFactory must be type of XML");
		((XMLDaoFactory) mSingelton).read();
	}

	public void flushOnChange(boolean flushOnChange) {
		mFlushOnChange = flushOnChange;
	}

	public void flush() throws DaoException {
		flush(mXmlFile);
	}

	public void flush(File xmlFile) throws DaoException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement(mRootName);

			for (Dao<?> dao : mDaoList)
				if (dao instanceof XMLDao<?>)
					((XMLDao<?>) dao).initWriteXML();

			doc.appendChild(root);
			for (Dao<?> dao : mDaoList)
				if (dao instanceof XMLDao<?>)
					((XMLDao<?>) dao).writeXML(doc, root);

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
			for (Dao<?> dao : mMapClassDao.values())
				if (dao instanceof XMLDao<?>)
					((XMLDao<?>) dao).initeadXML(doc);
			
			for (Dao<?> dao : mMapClassDao.values())
				if (dao instanceof XMLDao<?>)
					((XMLDao<?>) dao).readXML(doc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new DaoException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	public void notifyChange() throws DaoException {
		if (mFlushOnChange)
			flush();
	}

}
