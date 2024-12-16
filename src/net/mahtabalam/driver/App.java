package net.mahtabalam.driver;

import java.time.LocalDateTime;

import net.mahtabalam.dao.MessageDAO;
import net.mahtabalam.dao.UserDAO;
import net.mahtabalam.db.seeder.RecordSeeder;

public class App {

	public static void main(String[] args) {
		final int USERS_COUNT = 1000;
		final int MESSAGES_COUNT = 5000;
		
		System.out.println("Inserting records into users table ...");
		RecordSeeder.addUsers(USERS_COUNT);
		System.out.println("Inserted " + USERS_COUNT + " users into DB");
		System.out.println("Inserting records into messages table ...");
		RecordSeeder.addMessages(MESSAGES_COUNT, USERS_COUNT);
		System.out.println("Inserted " + MESSAGES_COUNT + " messages into DB");
		
		
		UserDAO userDAO = new UserDAO();
		userDAO.insertUser(6971, "Some Name", "some_username");
		System.out.println(userDAO.getUserById(6971));
		
		MessageDAO messageDAO = new MessageDAO();
		messageDAO.insertMessage(879982, 6971, 811, "Some random message", LocalDateTime.now());
		System.out.println(messageDAO.getMessagesBySender(6971));
		
	}

}
