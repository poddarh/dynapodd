package org.dynapodd.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

public class BasicDAO {
	private SessionFactory sessionFactory;
	
	public BasicDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	enum Direction {
		ASCENDING, DESCENDING
	}

	/**
	 * @param object
	 * @return List of the records matching the non-null fields of object
	 *         supplied
	 */
	public <T> List<T> find(T object) {
		return find(object, null, null, 0, 0);
	}

	/**
	 * @param object
	 * @param limit
	 *            sets the maximum number of records to retrieve
	 * @param offset
	 *            sets the no. of records to skip
	 * @return List of the records matching the non-null fields of object
	 *         supplied
	 */
	public <T> List<T> find(T object, int limit, int offset) {
		return find(object, null, null, limit, offset);
	}

	/**
	 * @param object
	 * @param sortColumn
	 *            specifies the field to sort by
	 * @param direction
	 *            specifies the sort direction
	 * @return List of the records matching the non-null fields of object
	 *         supplied
	 */
	public <T> List<T> find(T object, String sortColumn, Direction direction) {
		return find(object, sortColumn, direction, 0, 0);
	}

	/**
	 * @param object
	 * @param sortColumn
	 *            specifies the field to sort by
	 * @param direction
	 *            specifies the sort direction
	 * @param limit
	 *            sets the maximum number of records to retrieve
	 * @param offset
	 *            sets the no. of records to skip
	 * @return List of the records matching the non-null fields of object
	 *         supplied
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(T object, String sortColumn, Direction direction,
			int limit, int offset) {

		Session session = getSession();
		Criteria criteria = DAOUtil.createCriteria(object, session);

		if (sortColumn != null && direction != null) {
			switch (direction) {
			case ASCENDING:
				criteria.addOrder(Order.asc(sortColumn));
				break;
			case DESCENDING:
				criteria.addOrder(Order.desc(sortColumn));
				break;
			}
		}
		if (offset > 0)
			criteria.setFirstResult(offset);

		if (limit > 0)
			criteria.setMaxResults(limit);

		List<T> list = criteria.list();
		closeSession(session);
		return list;
	}

	/**
	 * @param id
	 *            of the desired record
	 * @param type
	 *            the class of the desired record
	 * @return the specific record
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(int id, Class<T> type) {
		Session session = getSession();
		Criteria criteria = DAOUtil.createCriteriaFromId(id, type, session);
		T t = (T) criteria.uniqueResult();
		closeSession(session);
		return t;
	}

	/**
	 * @param object
	 * @return the record matching the non-null fields of object supplied
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T object) {
		Session session = getSession();
		Criteria criteria = DAOUtil.createCriteria(object, session);
		T t = (T) criteria.uniqueResult();
		closeSession(session);
		return t;
	}

	/**
	 * @param object
	 * @return the number of records matching the non-null fields of object
	 *         supplied
	 */
	public <T> long count(T object) {
		Session session = getSession();
		Criteria criteria = DAOUtil.createCriteria(object, session);
		criteria.setProjection(Projections.rowCount());
		Number count = (Number) criteria.uniqueResult();
		closeSession(session);
		return (Long) count;

	}

	/**
	 * mimics the <strong>saveOrUpdate</strong> function of the session class
	 */
	public <T> void saveOrUpdate(T object) {
		Session session = getSession();
		session.saveOrUpdate(object);
		closeSession(session);
	}

	/**
	 * mimics the <strong>save</strong> function of the session class
	 */
	public <T> void save(T object) {
		Session session = getSession();
		session.save(object);
		closeSession(session);
	}

	/**
	 * Updates a record by ID. The non-null fields of the updateObject are
	 * replaced to the fields on current record
	 */
	@SuppressWarnings("unchecked")
	public <T> void updateByID(int id, T dataObject) {
		T existingObject = (T) findOne(id, dataObject.getClass());

		Session session = getSession();
		DAOUtil.updateObject(existingObject,dataObject);
		session.saveOrUpdate(existingObject);
		closeSession(session);
	}

	/**
	 * Finds all records matching the updateObject and updates the values from
	 * the updateObject's non-null fields
	 */
	public <T> void update(T queryObject, T updateObject) {
		List<T> existingObjects = find(queryObject);
		Session session = getSession();
		DAOUtil.updateObjects(existingObjects, updateObject);
		existingObjects.forEach(session::saveOrUpdate);
		closeSession(session);
	}

	/**
	 * Deletes a record. Mimics the session's delete method.
	 */
	public <T> void delete(T object) {
		Session session = getSession();
		session.delete(object);
		closeSession(session);
	}
	
	/**
	 * Deletes the record with the matching ID
	 */
	public <T> void delete(int id, Class<T> type) {
		Session session = getSession();
		session.delete(findOne(id, type));
		closeSession(session);
	}

	/**
	 * USE WITH CAUTION! Deletes all records matching object. If an empty object
	 * passed, may erase the entire collection.
	 */
	public <T> void deleteMany(T queryObject) {
		Session session = getSession();
		find(queryObject).forEach(session::delete);
		closeSession(session);
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Session getSession() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		return session;
	}

	public void closeSession(Session session) {
		if (session.getTransaction().isActive())
			session.getTransaction().commit();
		session.close();
	}
}
