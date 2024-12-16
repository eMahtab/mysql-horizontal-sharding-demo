package net.mahtabalam.db.seeder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import net.mahtabalam.shards.ShardManager;

public class RecordSeeder {
	
	private static final String INSERT_USER_QUERY = "INSERT INTO users (id, name, username) VALUES (?, ?, ?);";
	private static final String INSERT_MESSAGE_QUERY = "INSERT INTO messages (id, sender_id, recipient_id, message, created_at) VALUES (?, ?, ?, ?, ?);";
	
	public static void addUsers(int userCount) {
        for (long id = 1; id <= userCount; id++) {
            String name = "User " + id;
            String username = "user_" + id;

            try {
                // Determine the shard connection based on the user ID
                Connection connection = ShardManager.getShardConnection(id);
                try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_QUERY)) {
                    statement.setLong(1, id);
                    statement.setString(2, name);
                    statement.setString(3, username);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Failed to insert user with ID: " + id);
                e.printStackTrace();
            }
        }
    }
	
	public static void addMessages(int messageCount, int userCount) {
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (long id = 1; id <= messageCount; id++) {
            long senderId = 1 + random.nextInt(userCount);
            long recipientId;
            do {
                recipientId = 1 + random.nextInt(userCount);
            } while (recipientId == senderId); // Ensure sender and recipient are different

            String message = "Message " + id;
            String createdAt = LocalDateTime.now().format(formatter);

            try {
                // Determine the shard connection based on the sender ID
                Connection connection = ShardManager.getShardConnection(senderId);
                try (PreparedStatement statement = connection.prepareStatement(INSERT_MESSAGE_QUERY)) {
                    statement.setLong(1, id);
                    statement.setLong(2, senderId);
                    statement.setLong(3, recipientId);
                    statement.setString(4, message);
                    statement.setString(5, createdAt);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Failed to insert message with ID: " + id);
                e.printStackTrace();
            }
        }
    }
}
