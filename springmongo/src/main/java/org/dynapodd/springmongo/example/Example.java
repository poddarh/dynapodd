package org.dynapodd.springmongo.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Example {
	
	public static void main(String[] args) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		
		UserService userService = context.getBean(UserService.class);
		
		User user = new User("Some Name", "abc@example.com", "password", "customer");
		userService.insert(user);
		
		user = userService.login("abc@example.com", "password");
		if(user!=null)
			userService.updateName(user.getUserId(), "Random Tandom");
		
	}
	
}
