package org.dynapodd.springmongo.example;

import org.dynapodd.springmongo.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class UserService {
	@Autowired
	private BasicDAO basicDAO;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	// The old way of getting a user record
	public User oldLogin(String email, String password) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(email));
		query.addCriteria(Criteria.where("password").is(password));
		return mongoTemplate.findOne(query, User.class);
		
	}
	
	
	// Getting a user record using Dynapodd
	public User login(String email, String password) {
		
		User user = new User();
		user.setEmail(email);
		user.setPassword(password);
		return basicDAO.findOne(user);
		
	}
	
	
	// Updating a user record
	public void updateName(String userID, String name) {
		
		User user = new User();
		user.setName(name);
		basicDAO.updateByID(userID, user);
		
	}
	
	
	// Inserting a new record
	public void insert(User user) {
		basicDAO.save(user);
	}
	
}