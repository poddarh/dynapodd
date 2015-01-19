package org.dynapodd.hibernate;

import java.beans.Statement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DAOUtil {
	private static Map<Class<?>, List<Property>> propertiesMap;
	private static Map<Class<?>, Property> idPropertyMap;

	static {
		propertiesMap = new HashMap<>();
		idPropertyMap = new HashMap<>();
	}

	/**
	 * @return a criteria object to find id as supplied in the table specified
	 *         by the class
	 */
	public static Criteria createCriteriaFromId(int id, Class<?> type,
			Session session) {
		Criteria criteria = session.createCriteria(type);
		criteria.add(Restrictions.eq(getIdProperty(type).fieldName, id));
		return criteria;
	}

	/**
	 * @param object
	 * @return a criteria object matching all the non-null objects in the object
	 *         supplied
	 */
	public static Criteria createCriteria(Object object, Session session) {
		Criteria criteria = session.createCriteria(object.getClass());
		Map<Property, Object> fields = getNonNullFields(object);
		fields.forEach((field,value) -> criteria.add(Restrictions.eq(field.fieldName, value)));
		return criteria;
	}

	/**
	 * Copies the non-null values from the dataObject to the oldObject
	 */
	public static void updateObject(Object oldObject, Object dataObject) {

		Class<?> type = dataObject.getClass();
		if (!propertiesMap.containsKey(type))
			extractProperties(type);
		Map<Property, Object> fields = getNonNullFields(dataObject);
		
		Property id = idPropertyMap.get(dataObject.getClass());
		
		fields.forEach((field,value) -> {
			if (id != field) {
				
				Statement s = new Statement(dataObject, getSetterName(field.fieldName), new Object[] { value });
				try {
					s.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	/**
	 * Copies the non-null values from the dataObject to all the oldObjects
	 */
	public static void updateObjects(List<? extends Object> existingObjects, Object dataObject) {
		
		Class<?> type = dataObject.getClass();
		if (!propertiesMap.containsKey(type))
			extractProperties(type);
		
		Map<Property, Object> fields = getNonNullFields(dataObject);

		Property id = idPropertyMap.get(dataObject.getClass());
		
		for (Object oldObject : existingObjects) {
			fields.forEach((field,value) -> {
				if (id != field) {
					
					Statement s = new Statement(oldObject, getSetterName(field.fieldName), new Object[] { value });
					try {
						s.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			});
		}
	}

	/**
	 * Searches for all nun-null fields in a object class and returns a Map with
	 * key as field names and values with the value stored in them.
	 */
	private static Map<Property, Object> getNonNullFields(Object object) {
		Class<?> type = object.getClass();
		Map<Property, Object> nonNullFields = new HashMap<>();

		List<Property> properties = propertiesMap.get(type);

		// Check if the fields for the class of object has been identified. If
		// not, load the field and method names for this class type.
		if(properties==null)
			properties = extractProperties(type);
			
		// Check all the field value for the object object for non-null fields
		for (Property property : properties) {

			Object value = null;
			// Get the value stored in a field.
			try {
				value = property.getter.invoke(object);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Check if the field is an instance of String and its value is not
			// "" or null. If it is not, then add it to the map.
			if (value instanceof String) {
				if (((String) value).trim().length() != 0) {
					nonNullFields.put(property, value);
				}
			}

			// Check if the field value is not null. If it is not, then add it
			// to the map.
			else if (value != null)
				nonNullFields.put(property, value);
		}
		return nonNullFields;
	}

	/**
	 * Look for all non-transient member variables in the class and put them
	 * into a static map
	 * @return 
	 */
	private static List<Property> extractProperties(Class<?> type) {
		List<Property> properties = new ArrayList<>();
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			// Ignore all primitive types
			if (field.getType().isPrimitive())
				continue;
			Property property;
			try {
				property = new Property(field.getName(),
						type.getMethod(getGetterName(field.getName())));
				Annotation[] annotations = field.getAnnotations();
				// If field transient, ignore. If Id, then put in idMap also
				for (Annotation annotation : annotations) {
					if (annotation.annotationType() == Transient.class)
						continue;
					else if (annotation.annotationType() == Id.class)
						idPropertyMap.put(type, property);
				}
			} catch (Exception e) {
				continue;
			}
			properties.add(property);
		}
		propertiesMap.put(type, properties);
		return properties;
	}

	// Returns the get method name for a field
	private static String getGetterName(String fieldName) {
		char begin = Character.toUpperCase(fieldName.charAt(0));
		return "get" + begin + fieldName.substring(1);
	}

	// Returns the get method name for a field
	private static String getSetterName(String fieldName) {
		char begin = Character.toUpperCase(fieldName.charAt(0));
		return "set" + begin + fieldName.substring(1);
	}

	// Returns the ID field name for a object class type
	private static Property getIdProperty(Class<?> type) {
		if (!idPropertyMap.containsKey(type))
			extractProperties(type);
		return idPropertyMap.get(type);
	}

}

class Property {
	String fieldName;
	Method getter;

	public Property() {
	}

	public Property(String fieldName, Method getter) {
		this.fieldName = fieldName;
		this.getter = getter;
	}
}
