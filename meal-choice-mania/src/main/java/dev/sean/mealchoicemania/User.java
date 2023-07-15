package dev.sean.mealchoicemania;

public class User {
	private int id;
	private String username;
	private String email;
//	private String password;
	
	public User(int id, String email, String username) {
		this.id = id;
		this.email = email;
		this.username = username;
	}
	
	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

}
