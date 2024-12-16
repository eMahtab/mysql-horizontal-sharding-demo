package net.mahtabalam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.mahtabalam.entity.Message;
import net.mahtabalam.shards.ShardManager;

public class MessageDAO {
	
	public void insertMessage(long messageId, long senderId, long recipientId, String messageText, LocalDateTime createdAt) {
	    String query = "INSERT INTO messages (id, sender_id, recipient_id, message, created_at) VALUES (?, ?, ?, ?, ?)";
	    try {
	        Connection connection = ShardManager.getShardConnection(senderId); // Use senderId for sharding
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setLong(1, messageId);
	            statement.setLong(2, senderId);
	            statement.setLong(3, recipientId);
	            statement.setString(4, messageText);
	            statement.setTimestamp(5, Timestamp.valueOf(createdAt));
	            statement.executeUpdate();
	        }
	    } catch (SQLException e) {
	        System.err.println("Failed to insert message with ID: " + messageId);
	        e.printStackTrace();
	    }
	}
	
	
	public List<Message> getMessagesBySender(long senderId) {
	    String query = "SELECT id, sender_id, recipient_id, message, created_at, edited_at, deleted_at "
	                 + "FROM messages WHERE sender_id = ?";
	    List<Message> messages = new ArrayList<>();
	    try {
	        Connection connection = ShardManager.getShardConnection(senderId); // Use senderId for sharding
	        try (PreparedStatement statement = connection.prepareStatement(query)) {
	            statement.setLong(1, senderId);
	            try (ResultSet rs = statement.executeQuery()) {
	                while (rs.next()) {
	                    messages.add(new Message(
	                        rs.getLong("id"),
	                        rs.getLong("sender_id"),
	                        rs.getLong("recipient_id"),
	                        rs.getString("message"),
	                        rs.getTimestamp("created_at").toLocalDateTime(),
	                        rs.getTimestamp("edited_at") != null ? rs.getTimestamp("edited_at").toLocalDateTime() : null,
	                        rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toLocalDateTime() : null
	                    ));
	                }
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Failed to fetch messages for sender ID: " + senderId);
	        e.printStackTrace();
	    }
	    return messages;
	}

}
