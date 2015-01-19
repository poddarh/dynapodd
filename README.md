# Dynapodd

Dynapodd makes database calls simpler. Currently, there are support classes for the **Hibernate** framework and the **Spring Data MongoDB** framework. Using Dynapodd, one can eliminate writing most queries. Methods in the BasicDAO class of Dynapodd generates and executes queries from an object’s non-null fields. If passed in a 'user' object with email and password fields with some value and rest null, it will write a query to find all 'similar' records (record with the email and password as passed in the object). A regular POJO can be used with this, although there are some restrictions and rules:

  - A model class cannot have any primitive members - rather use wrapper classes
  - The ID member should be annotated with _org.springframework.data.annotation.Id_ (when using Spring Data) or with _javax.persistence.Id_ (when using Hibernate). This is mandatory even if XML based configuration already mentions so.
  - A member that is not part of table/collection has to be annotated with _org.springframework.data.annotation.Transient_ (when using Spring Data) or with _javax.persistence.Transient_ (when using Hibernate). Just like ID, this is mandatory as well.
  - Hibernate ID member should be of type _java.lang.Integer_ and Spring Data ID member should be of _java.lang.String_ type.

### Spring Data MongoDB 
#####Installation
You may either copy the classes BasicDAO and DAOUtil of the _org.dynapodd.springmongo_ package or you may [download the jar](http://poddarh.github.io/dynapodd/jars/springmongo.jar)

#####Usage
The BasicDAO needs to be initiated using Spring's DI as a singleton object. The MongoTemplate object in BasicDAO is annotated Autowired. It simplifies the basic CRUD operations. The DAOUtil class has methods that may help in custom methods other than the common ones in the BasicDAO.

**Example**: The conventional way of finding a user record by its username and password.

```
Query query = new Query();
query.addCriteria(Criteria.where("email").is(email));
query.addCriteria(Criteria.where("password").is(password));
User user =  mongoTemplate.findOne(query, User.class);
```
... now using Dynapodd ...
```
User user = new User();
user.setEmail(email);
user.setPassword(password);
user = basicDAO.findOne(user);
```

#####Available methods

- **List&lt;T&gt; find(T object)**: Find all similar objects
- **List&lt;T&gt; find(T object, int limit, int offset)**: Find similar objects with pagination
- **List&lt;T&gt; find(T object, String sortColumn, Direction direction)**: Find all similar objects in a sorted list
- **List&lt;T&gt; find(T object, String sortColumn, Direction direction, int limit, int offset)**: Find similar objects in a sorted list with pagination
- **T findOne(T object)**: Find a single similar object. Returns the first match found
- **long count(T object)**: Returns a count of similar objects
- **void save(T object)**: Saves an object (mimics the MongoTemplate save method)
- **void insert(T object)**: Inserts an object (mimics the MongoTemplate insert method)
- **boolean updateByID(String id, T object)**: Updates the record with the id specified with the non-null fields in the object
- **boolean update(T queryObject, T dataObject)**: Updates all records similar to queryObject with the non-null fields in the dataObject
- **void remove(T object)**: Removes a record by ID in the object (mimics the MongoTemplate remove method)
- **void removeMany(T object)**: Removes all similar records permanently from the database

### Hibernate

#####Installation
You may either copy the classes BasicDAO and DAOUtil of the _org.dynapodd.hibernate_ package or you may [download the jar](http://poddarh.github.io/dynapodd/jars/hibernate.jar)

#####Usage
The BasicDAO needs to be initiated by passing an instance of 'org.hibernate.SessionFactory' to its constructor. This shall be used as a singleton object. It simplifies the basic CRUD operations. The DAOUtil class has methods that may help in custom methods other than the common ones in the BasicDAO.

**Example**: The conventional way of finding a user record by its username and password.

```
Session session = sessionFactory.openSession();
session.beginTransaction();
		
Criteria criteria = session.createCriteria(User.class);
criteria.add(Restrictions.eq("email", email));
criteria.add(Restrictions.eq("password", password));
User user = (User) criteria.uniqueResult();
		
session.getTransaction().commit();
session.close();
```
... now using Dynapodd ...
```
User user = new User();
user.setEmail(email);
user.setPassword(password);
user = basicDAO.findOne(user);
```

#####Available methods

- **List&lt;T&gt; find(T object)**: Find all similar objects
- **List&lt;T&gt; find(T object, int limit, int offset)**: Find similar objects with pagination
- **List&lt;T&gt; find(T object, String sortColumn, Direction direction)**: Find all similar objects in a sorted list
- **List&lt;T&gt; find(T object, String sortColumn, Direction direction, int limit, int offset)**: Find similar objects in a sorted list with pagination
- **T findOne(int id,  Class&lt;T&gt; type)**: Find an object with the matching I   
- **T findOne(T object)**: Find a single similar object. Returns the first match found
- **long count(T object)**: Returns a count of similar objects
- **void saveOrUpdate(T object)**: Saves or updates an object (mimics the Session saveOrUpdate method)
- **void save(T object)**: Saves an object (mimics the Session save method)
- **void update(T queryObject, T dataObject)**: Updates all records similar to queryObject with the non-null fields in the dataObject
- **void updateByID(int id, T dataObject)**: Updates record with same id with the non-null fields in the dataObject
- **void delete(T object)**: Deletes a record. (mimics the Session delete method)
- **void delete(int id,  Class&lt;T&gt; type)**: Finds a record and then deletes it
- **void deleteMany(T queryObject)**: Finds and then deletes all similar records
- **Session getSession()**: Opens a session, begins transaction and returns the session object
- **void closeSession(Session session)**:  Commits a transacton and then closes the session



For more info, please refer to the [javadoc]. 
[javadoc]:http://poddarh.github.io/dynapodd/javadoc

### Development

Please contribute to the project :)

License
----

MIT

