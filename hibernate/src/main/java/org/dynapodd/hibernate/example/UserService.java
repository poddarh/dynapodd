package org.dynapodd.hibernate.example;

import org.dynapodd.hibernate.BasicDAO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

public class UserService {
	
	private SessionFactory sessionFactory;
	private BasicDAO basicDAO;
	
	// Constructor to initialize SessionFactory and BasicDAO. This can also be achieved using dependency injection.
	public UserService() {
		
		// Hibernate Configuration
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
		sessionFactory = configuration.buildSessionFactory(builder.build());
		
		// Instantiate BasicDAO object using sessionFactory
		basicDAO = new BasicDAO(sessionFactory);
		
	}
	
	// The old way of getting a user record
	public User oldLogin(String email, String password) {
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		Criteria criteria = session.createCriteria(User.class);
		criteria.add(Restrictions.eq("email", email));
		criteria.add(Restrictions.eq("password", password));
		User user = (User) criteria.uniqueResult();
		
		session.getTransaction().commit();
		session.close();
		
		return user;
	}
	
	// Getting a user record using Dynapodd
	public User login(String email, String password) {
		
		User user = new User();
		user.setEmail(email);
		user.setPassword(password);
		return basicDAO.findOne(user);
		
	}
	
	// Updating a user record
	public void updateName(Integer userID, String name) {
		
		User user = new User();
		user.setName(name);
		basicDAO.updateByID(userID, user);
		
	}
	
	// Inserting a new record
	public void insert(User user) {
		basicDAO.save(user);
	}
	
}