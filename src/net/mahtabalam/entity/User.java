package net.mahtabalam.entity;

public class User {
	private long id;
	private String name;
	private String username;

	public User(long id, String name, String username) {
		this.id = id;
		this.name = name;
		this.username = username;
	}

	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", username=" + username + "]";
	}
}
