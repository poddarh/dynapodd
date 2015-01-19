package org.dynapodd.hibernate.example;

public class Example {
	
	public static void main(String[] args) {
		
		UserService userService = new UserService();
		
		User user = new User("Some Name", "abc@example.com", "password", "customer");
		userService.insert(user);
		
		user = userService.oldLogin("abc@example.com", "password");
		if(user!=null)
			userService.updateName(user.getUserId(), "Random Tandom");
		
	}
	
}
