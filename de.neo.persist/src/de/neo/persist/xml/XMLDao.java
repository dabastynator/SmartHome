package de.neo.persist.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.neo.persist.Dao;
import de.neo.persist.DaoException;
import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Id;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.OneToMany;
import de.neo.persist.annotations.Persist;

public class XMLDao<T> implements Dao<T> {

	private static Random mRandom = new Random(System.currentTimeMillis());

	private Class<? extends T> mClass;

	private Map<Long, T> mDomains = new HashMap<>();
	private List<T> mDomainList = new ArrayList<>();

	private Set<Long> mAlreadySaved = new HashSet<>();

	private List<PersistentField> mPersistentFields;

	private String mName;

	private Field mIDField;

	private XMLDaoFactory mFactory;

	private Method mOnCreateMethod;

	private Map<T, Long> mIdMap = new HashMap<>();

	public XMLDao(Class<? extends T> c) throws DaoException {
		mClass = c;
		Domain d = c.getAnnotation(Domain.class);
		if (d == null)
			throw new DaoException("Domain annotation missing for " + c.getSimpleName());
		mName = d.name();
		if (mName.equals(""))
			mName = c.getSimpleName();

		for (Method m : c.getDeclaredMethods()) {
			if (m.getAnnotation(OnLoad.class) != null && m.getParameterTypes().length == 0) {
				mOnCreateMethod = m;
				mOnCreateMethod.setAccessible(true);
			}
		}

		initializeFields();
	}

	public void setFactory(XMLDaoFactory factory) {
		mFactory = factory;
		for (PersistentField f : mPersistentFields)
			f.setFactory(mFactory);
	}

	private void initializeFields() throws DaoException {
		mPersistentFields = new ArrayList<>();
		ArrayList<Field> allFields = new ArrayList<>();
		fillAllFieldsOf(mClass, allFields);
		for (Field f : allFields) {
			if (f.getAnnotation(Id.class) != null)
				mIDField = f;

			Persist p = f.getAnnotation(Persist.class);
			OneToMany oneToMany = f.getAnnotation(OneToMany.class);
			if (p != null || mIDField == f || oneToMany != null) {
				if (f.getType().equals(Integer.class) || f.getType().equals(int.class))
					mPersistentFields.add(new IntegerField(f, p));
				else if (f.getType().equals(Boolean.class) || f.getType().equals(boolean.class))
					mPersistentFields.add(new BooleanField(f, p));
				else if (f.getType().equals(Double.class) || f.getType().equals(double.class))
					mPersistentFields.add(new DoubleField(f, p));
				else if (f.getType().equals(Long.class) || f.getType().equals(long.class))
					mPersistentFields.add(new LongField(f, p));
				else if (f.getType().equals(Float.class) || f.getType().equals(float.class))
					mPersistentFields.add(new FloatField(f, p));
				else if (f.getType().equals(String.class))
					mPersistentFields.add(new StringField(f, p));
				else if (f.getType().getAnnotation(Domain.class) != null)
					mPersistentFields.add(new DomainField(f, p));
				else if (f.getType() instanceof Class && ((Class<?>) f.getType()).isEnum())
					mPersistentFields.add(new EnumField(f, p));
				else if (oneToMany != null) {
					if (oneToMany.domainClass().getAnnotation(Domain.class) == null)
						throw new DaoException("OneToMany reference must reference to domain class.");
					mPersistentFields.add(new DomainListField(f, p, oneToMany));
				} else
					throw new DaoException("Not supported persistent field: " + f.getName() + " ("
							+ f.getType().getSimpleName() + ")");
			}
		}
		if (mIDField != null) {
			if (!(mIDField.getType().equals(Long.class) || mIDField.getType().equals(long.class)))
				throw new DaoException("Id field must be type of long");
			mIDField.setAccessible(true);
		}
	}

	private void fillAllFieldsOf(Class<?> c, ArrayList<Field> list) {
		for (Field f : c.getDeclaredFields())
			list.add(f);
		if (c.getSuperclass() != null && !c.getSuperclass().equals(Object.class))
			fillAllFieldsOf(c.getSuperclass(), list);
	}

	@Override
	public List<T> loadAll() throws DaoException {
		return new ArrayList<>(mDomainList);
	}

	@Override
	public T loadById(long id) throws DaoException {
		return mDomains.get(id);
	}

	@Override
	public long count() throws DaoException {
		return mDomains.size();
	}

	@Override
	public long save(T item) throws DaoException
	{
		long id = nextId();
		try
		{
			setId(item, id);
		} catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new DaoException("Error accessing the id field");
		}
		mDomains.put(id, item);
		if (!mDomainList.contains(item))
		{
			mDomainList.add(item);
		}
		mFactory.notifyChange();
		return id;
	}

	private long nextId() {
		long id = Math.abs(mRandom.nextLong());
		while (mDomains.containsKey(id))
			id = Math.max(0, id + 1);
		return id;
	}

	@Override
	public void update(T item) throws DaoException {
		try {
			Long oldId = getId(item);
			if (oldId == null)
				throw new DaoException("Can't update unknown item " + item);
			mDomains.put(oldId, item);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DaoException("Error accessing id field");
		}
		mFactory.notifyChange();
	}

	@Override
	public synchronized void delete(long id) throws DaoException {
		for (int i = mDomainList.size()-1; i >= 0; i--)
		{
			T domain = mDomainList.get(i);
			try {
				if (getId(domain) == id)
				{
					mDomainList.remove(i);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new DaoException("Error accessing id for " + mName + ". " + e.getClass().getSimpleName()
						+ ": " + e.getMessage());
			}
		}
		mDomains.remove(id);
		mFactory.notifyChange();
	}

	@Override
	public void deleteAll() throws DaoException {
		mDomains.clear();
		mDomainList.clear();
		mIdMap.clear();
		mFactory.notifyChange();
	}

	public void readXML(Document doc) throws DaoException {
		NodeList list = doc.getFirstChild().getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item instanceof Element && ((Element) item).getTagName().equals(mName)) {
				try {
					readDomainXML((Element) item);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new DaoException("Error reading the xml for " + mName + ". " + e.getClass().getSimpleName()
							+ ": " + e.getMessage());
				}

			}
		}
	}

	protected T readDomainXML(Element e) throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, DaoException, InvocationTargetException, NoSuchMethodException, SecurityException {
		T object = mClass.getDeclaredConstructor().newInstance();
		for (PersistentField f : mPersistentFields)
			f.setValueToObject(object, e);
		Long id = getId(object);
		if (mDomains.containsKey(id))
			return mDomains.get(id);
		mDomains.put(id, object);
		mDomainList.add(object);
		if (mOnCreateMethod != null) {
			mOnCreateMethod.invoke(object);
		}
		return object;
	}

	protected Long getId(T object) throws IllegalArgumentException, IllegalAccessException {
		if (mIDField != null) {
			Object value = mIDField.get(object);
			return (Long) value;
		} else {
			if (mIdMap.containsKey(object))
				return mIdMap.get(object);
			long id = nextId();
			mIdMap.put(object, id);
			return id;
		}
	}

	private void setId(T object, long id) throws IllegalArgumentException, IllegalAccessException {
		if (mIDField != null) {
			mIDField.setLong(object, id);
		} else
			mIdMap.put(object, id);
	}

	public void writeXML(Document doc, Element root)
			throws IllegalArgumentException, IllegalAccessException, DaoException {
		for (T domain : mDomains.values()) {
			if (!alreadySaved(domain))
				writeDomainXML(doc, root, domain);
		}
	}

	public boolean alreadySaved(T domain) throws IllegalArgumentException, IllegalAccessException {
		return mAlreadySaved.contains(getId(domain));
	}

	protected Element writeDomainXML(Document doc, Element root, T domain)
			throws IllegalArgumentException, IllegalAccessException, DaoException {
		Element element = doc.createElement(mName);
		root.appendChild(element);
		mAlreadySaved.add(getId(domain));
		for (PersistentField f : mPersistentFields)
			f.setValueToXML(doc, domain, element);
		return element;
	}

	protected void initWriteXML() {
		mAlreadySaved.clear();
	}

	public void initReadXML(Document doc) {
		mDomains.clear();
	}

	@Override
	public Class<?> getDomainClass() {
		return mClass;
	}

	@Override
	public void delete(T item) throws DaoException {
		try {
			delete(getId(item));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DaoException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

}
