package net.mahtabalam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mahtabalam.entity.User;
import net.mahtabalam.shards.ShardManager;

public class UserDAO {
	
	public void insertUser(long userId, String name, String username) {
	    String query = "INSERT INTO users (id, name, username) VALUES (?, ?, ?)";
	    try {
	        Connection connection = ShardManager.getShardConnection(userId); // Use userId for sharding
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setLong(1, userId);
	            statement.setString(2, name);
	            statement.setString(3, username);
	            statement.executeUpdate();
	        }
	    } catch (SQLException e) {
	        System.err.println("Failed to insert user with ID: " + userId);
	        e.printStackTrace();
	    }
	}
	
	
	public User getUserById(long userId) {
	    String query = "SELECT id, name, username FROM users WHERE id = ?";
	    User user = null;
	    try {
	        Connection connection = ShardManager.getShardConnection(userId); // Use userId for sharding
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setLong(1, userId);
	            try (ResultSet rs = statement.executeQuery()) {
	                if (rs.next()) {
	                    user = new User(
	                        rs.getLong("id"),
	                        rs.getString("name"),
	                        rs.getString("username")
	                    );
	                }
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Failed to fetch user with ID: " + userId);
	        e.printStackTrace();
	    }
	    return user;
	}



}
