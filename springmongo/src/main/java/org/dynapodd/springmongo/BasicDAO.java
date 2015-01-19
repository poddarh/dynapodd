package org.dynapodd.springmongo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class BasicDAO {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	static Direction ASCENDING = Sort.Direction.ASC;
	static Direction DESCENDING = Sort.Direction.DESC;

	/**
	 * @param object
	 * @return List of all the records matching the object supplied
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
	 * @return List of the records matching the object supplied.
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
	 *            sets the no. of records to skip.
	 * @return List of the records matching the object supplied.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(T object, String sortColumn, Direction direction,
			int limit, int offset) {
		Query query = DAOUtil.getFieldsQuery(object);
		if (sortColumn != null && direction != null)
			query.with(new Sort(direction, sortColumn));
		if (limit != 0)
			query.limit(limit);
		if (offset != 0)
			query.skip(offset);
		return (List<T>) mongoTemplate.find(query, object.getClass());
	}

	/**
	 * @param object
	 * @return the record matching the non-null fields of object supplied
	 */
	@SuppressWarnings("unchecked")
	public <T> T findOne(T object) {
		return (T) mongoTemplate.findOne(DAOUtil.getFieldsQuery(object),
				object.getClass());
	}

	/**
	 * @param object
	 * @return the number of records matching the non-null fields of object
	 *         supplied
	 */
	public <T> long count(T object) {
		return mongoTemplate.count(DAOUtil.getFieldsQuery(object),
				object.getClass());
	}

	/**
	 * Saves a record
	 * 
	 * @param object
	 */
	public <T> void save(T object) {
		mongoTemplate.save(object);
	}

	/**
	 * Inserts a new record
	 * 
	 * @param object
	 */
	public <T> void insert(T object) {
		mongoTemplate.insert(object);
	}

	/**
	 * Updates a record by ID
	 * @returns boolean identifying if 1 or more records were modified
	 */
	public <T> boolean updateByID(String id, T object) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id));
		Update update = DAOUtil.getFieldsUpdate(object);
		if (update != null)
			return mongoTemplate.updateFirst(query, update, object.getClass())
					.getN() > 0;
		else
			return false;
	}

	/**
	 * Finds all records matching the queryObject and updates the values from
	 * the updateObject's non-null fields
	 * @returns boolean identifying if 1 or more records were modified
	 */
	public <T> boolean update(T queryObject, T updateObject) {
		Update update = DAOUtil.getFieldsUpdate(updateObject);
		if (update != null)
			return mongoTemplate.updateMulti(DAOUtil.getFieldsQuery(queryObject),
					update, queryObject.getClass()).getN() > 0;
		else
			return true;
	}

	/**
	 * Deletes the record with the matching ID
	 */
	public <T> void remove(T object) {
		mongoTemplate.remove(object);
	}

	/**
	 * USE WITH CAUTION! Deletes all records matching object. If an empty object
	 * passed, may erase the entire collection.
	 */
	public <T> void removeMany(T object) {
		mongoTemplate.remove(DAOUtil.getFieldsQuery(object), object.getClass());
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
}
