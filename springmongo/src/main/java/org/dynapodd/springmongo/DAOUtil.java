package org.dynapodd.springmongo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class DAOUtil {
	private static Map<Class<?>, List<Property>> propertiesMap;
	private static Map<Class<?>, Property> idPropertyMap;

	static {
		propertiesMap = new HashMap<>();
		idPropertyMap = new HashMap<>();
	}
	
	/**
	 * @param object
	 * @return a Query object with criteria matching all the non-null objects in the object supplied
	 */
	public static Query getFieldsQuery(Object object) {
		Query query = new Query();
		Map<String, Object> fields = getNonNullFields(object);
		fields.forEach((field, value) -> query.addCriteria(Criteria.where(field).is(value)));
		return query;
	}

	/**
	 * @param object
	 * @return an Update object matching all the non-null objects in the object supplied
	 */
	public static Update getFieldsUpdate(Object object) {
		Map<String, Object> fields = getNonNullFields(object);
		if (fields.size() == 0)
			return null;
		
		Update update = new Update();
		String idFieldName = idPropertyMap.get(object.getClass()).fieldName;
		
		fields.forEach((field, value) -> {
			if(!idFieldName.equals(field))
				update.set(field,value);
		});
		return update;
	}
	
	/**
	 * @param object
	 * @return a map of all non-null fields in object with their responding values
	 */
	private static Map<String, Object> getNonNullFields(Object object) {
		Class<?> type = object.getClass();
		Map<String, Object> nonNullFields = new HashMap<>();
		List<Property> properties = propertiesMap.get(type);
		if (properties == null)
			properties = extractProperties(type);

		for (Property property : properties) {
			Object value = null;
			try {
				value = property.getter.invoke(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (value != null)
				nonNullFields.put(property.fieldName, value);
		}
		return nonNullFields;
	}
	
	/**
	 * Extracts, saves and returns a list of properties in a specific class
	 * 
	 * @param type the class object of the type
	 * @return a list of extracted properties
	 */
	private static List<Property> extractProperties(Class<?> type) {
		List<Property> properties = new ArrayList<>();
		Field[] fields = type.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.getType().isPrimitive())
				continue;
			Property property;
			try {
				property = new Property(field.getName(),
						type.getMethod(getGetMethodName(field.getName())));
				Annotation[] annotations = field.getAnnotations();
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
	
	/**
	 * @param fieldName
	 * @return the getter method's name based on the property name
	 */
	private static String getGetMethodName(String fieldName) {
		char begin = Character.toUpperCase(fieldName.charAt(0));
		return "get" + begin + fieldName.substring(1);
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
	
	@Override
	public boolean equals(Object obj) {
		return fieldName.equals(((Property) obj).fieldName);
	}
	
}