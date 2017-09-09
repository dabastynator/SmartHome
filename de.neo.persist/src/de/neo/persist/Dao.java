package de.neo.persist;

import java.util.List;

/**
 * 
 * The Dao interface specifies the basic functionality of all daos: load all
 * objects, load by specific id, save a new object, update an existing object
 * and delete objects.
 * 
 * @author sebastian
 * 
 * @param <T>
 */
public interface Dao<T> {

	/**
	 * Load all objects from the database.
	 * 
	 * @return list
	 * @throws DaoException
	 */
	public List<T> loadAll() throws DaoException;

	/**
	 * Load object with the specified id or null if the id is unknown.
	 * 
	 * @param id
	 * @return item
	 * @throws DaoException
	 */
	public T loadById(long id) throws DaoException;

	/**
	 * Count all items
	 * 
	 * @throws DaoException
	 */
	public long count() throws DaoException;

	/**
	 * Save the new item, set and return the generated id.
	 * 
	 * @param item
	 * @return id
	 * @throws DaoException
	 */
	public long save(T item) throws DaoException;

	/**
	 * Update persistent fields of the existing item.
	 * 
	 * @param item
	 * @throws DaoException
	 */
	public void update(T item) throws DaoException;

	/**
	 * Delete the object with the specified id.
	 * 
	 * @param id
	 * @throws DaoException
	 */
	public void delete(long id) throws DaoException;

	/**
	 * Delete all objects.
	 * 
	 * @throws DaoException
	 */
	public void deleteAll() throws DaoException;

	/**
	 * Get the domain class object.
	 * 
	 * @return domain class
	 */
	public Class<?> getDomainClass();
}
