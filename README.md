# MySQL Horizontal Sharding Demo

In this demo we will shard MySQL `test` database having two tables `users` and `messages`. 

We will create 4 shards, each of the shard will contain the test database and both the tables within it, but **each individual shard will only have a subset of the entire data, where data is distributed on different shards based on shard key.**


# Step 1 : Create, initialize the test database and run MySQL shards using Docker Compose
```yml
---
version: "2"
services:
  shard1:
    image: mysql:8.0
    container_name: app-shard-1
    restart: always
    volumes:
      - ./database-initialization.sql:/docker-entrypoint-initdb.d/database-initialization.sql
      - shard-1-data:/var/lib/mysql
    environment:
      &mysql-default-environment
      MYSQL_ROOT_PASSWORD: toor
      MYSQL_DATABASE: test
    ports:
      - "3308:3306"
  shard2:
    image: mysql:8.0
    container_name: app-shard-2
    restart: always
    volumes:
      - ./database-initialization.sql:/docker-entrypoint-initdb.d/database-initialization.sql
      - shard-2-data:/var/lib/mysql
    environment: *mysql-default-environment
    ports:
      - "3309:3306"
  shard3:
    image: mysql:8.0
    container_name: app-shard-3
    restart: always
    volumes:
      - ./database-initialization.sql:/docker-entrypoint-initdb.d/database-initialization.sql
      - shard-3-data:/var/lib/mysql
    environment: *mysql-default-environment
    ports:
      - "3310:3306"
  shard4:
    image: mysql:8.0
    container_name: app-shard-4
    restart: always
    volumes:
      - ./database-initialization.sql:/docker-entrypoint-initdb.d/database-initialization.sql
      - shard-4-data:/var/lib/mysql
    environment: *mysql-default-environment
    ports:
      - "3311:3306"        

volumes:
  shard-1-data:
  shard-2-data:
  shard-3-data:
  shard-4-data:
```


```sql
-- Initialize the test database and tables if they do not exist

-- Create the test database if it doesn't exist
CREATE DATABASE IF NOT EXISTS test;
USE test;

-- Create the 'users' table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    name VARCHAR(50),
    username VARCHAR(30),
    PRIMARY KEY (id)
);

-- Create the 'messages' table if it doesn't exist
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT NOT NULL,
    sender_id BIGINT,
    recipient_id BIGINT,
    message TEXT,
    created_at DATETIME NOT NULL,
    edited_at DATETIME DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);
```
**docker-compose.yml and database-initialization.sql**

!["docker-compose.yml and Database initialization script"](dockerfile-and-database-initialization.png?raw=true)

**docker compose up**

!["docker compose up"](docker-compose-up.png?raw=true)

**4 MySQL shards running as docker containers**

!["4 MySQL shards as docker containers"](mysql-shards-as-docker-containers.png?raw=true)

# Step 2 : Write the code to insert/get records from database shards based on shard key

## Shard Key : 

**We will shard the users table based on id (which refers to user id) and messages table based on sender_id.**

I created a maven project with below two dependencies

### dependencies in pom.xml
```xml
<dependencies>
    <dependency>
	    <groupId>org.yaml</groupId>
	    <artifactId>snakeyaml</artifactId>
	    <version>2.0</version> 
    </dependency>
    <dependency>
	    <groupId>mysql</groupId>
	    <artifactId>mysql-connector-java</artifactId>
	    <version>8.0.33</version>
    </dependency>
</dependencies>
```

### shards.yml
```yml
shards:
  - id: 0
    host: "localhost"
    port: 3308
    database: "test"
    username: "root"
    password: "toor"
  - id: 1
    host: "localhost"
    port: 3309
    database: "test"
    username: "root"
    password: "toor"
  - id: 2
    host: "localhost"
    port: 3310
    database: "test"
    username: "root"
    password: "toor"
  - id: 3
    host: "localhost"
    port: 3311
    database: "test"
    username: "root"
    password: "toor"
```

### ShardConfig.java
```java
package net.mahtabalam.shards;

public class ShardConfig {
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String database;
   
    public ShardConfig(String host, String port, String database, String username, String password) {
	this.host = host;
	this.port = port;
	this.username = username;
	this.password = password;
	this.database = database;
    }
	
    public String getHost() {
	return host;
    }
    public String getPort() {
	return port;
    }
    public String getUsername() {
	return username;
    }
    public String getPassword() {
	return password;
    }
    public String getDatabase() {
	return database;
    }
    public String getJdbcURL() {
	String jdbcURL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
	return jdbcURL;
    }
    public String toString() {
        return getJdbcURL();
    }
}
```

### ShardConfigLoader.java

It uses the [snakeyaml](https://mvnrepository.com/artifact/org.yaml/snakeyaml/2.3) to parse the shards.yml file and puts the shard info in a map where map key is the id of the shard.

```java
package net.mahtabalam.shards;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ShardConfigLoader {

    public static Map<Integer, ShardConfig> loadShardConfigs() {
        Map<Integer, ShardConfig> shardConfigs = new HashMap<>();
        try (InputStream input = ShardConfigLoader.class.getResourceAsStream("/shards.yml")) {
        	if (input == null) {
                throw new RuntimeException("shards.yml not found in the classpath");
            }
        	
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            List<Map<String, Object>> shards = (List<Map<String, Object>>) data.get("shards");
            for (Map<String, Object> shard : shards) {
                String host = (String) shard.get("host");
                int port = (int) shard.get("port");
                String database = (String) shard.get("database");
                String username = (String) shard.get("username");
                String password = (String) shard.get("password");

                ShardConfig shardConfig = new ShardConfig(host, String.valueOf(port), database, username, password);
                int shardId = (int) shard.get("id");
                shardConfigs.put(shardId, shardConfig);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shard configurations", e);
        }

        return shardConfigs;
    }
}
```

### ShardManager.java

ShardManager calls ShardConfigLoader to load the config and then **creates the database Connection object to each database shard and puts the database connection object in a map, where map key is the shard id.**

```java
package net.mahtabalam.shards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ShardManager {
    private static final Map<Integer, Connection> shardConnections = new HashMap<>();

    static {
        try {
            Map<Integer, ShardConfig> shardConfigs = ShardConfigLoader.loadShardConfigs();
            for (Map.Entry<Integer, ShardConfig> entry : shardConfigs.entrySet()) {
                int shardId = entry.getKey();
                ShardConfig config = entry.getValue();
                Connection connection = DriverManager.getConnection(
                        config.getJdbcURL(),
                        config.getUsername(),
                        config.getPassword()
                );
                shardConnections.put(shardId, connection);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize shard connections", e);
        }
    }

    public static Connection getShardConnection(long id) throws SQLException {
        int shardId = (int) (id % shardConnections.size());
        Connection connection = shardConnections.get(shardId);
        if (connection == null || connection.isClosed()) {
            throw new RuntimeException("No active connection found for shardId: " + shardId);
        }
        return connection;
    }
}
```
### RecordSeeder.java

It uses the ShardManager to get the database connection to the required shard (based on shard key) and uses that shard to insert records in the `users` and `messages` tables.

```java
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
```
!["Eclipse Project"](eclipse-project.png?raw=true)

# Step 3 : Execute the program (App.java main file) and verify shards

On running App.java, it inserts in total 1000 records into users table and 5000 records into messages table, the records are stored on different shards based on shard key (id for users table and sender_id for messages table).

We add one user to users table with id 6971, and then fetch that user from the database shard based on id.

We also add one message to messages table with id 879982, sender_id 6971 and recipient_id 811, and then we fetch that message from the database shard based on sender_id.

### Console Output on executing App.java
```
Inserting records into users table ...
Inserted 1000 users into DB
Inserting records into messages table ...
Inserted 5000 messages into DB
User [id=6971, name=Some Name, username=some_username]
[Message [id=879982, senderId=6971, recipientId=811, message=Some random message, createdAt=2024-12-16T16:05:39, editedAt=null, deletedAt=null]]
```

!["Number of Records on different shards"](records-on-shards.png?raw=true)

### Records on different shards

```
app-shard-1 : 250 users, 1224 messages
app-shard-2 : 250 users, 1254 messages
app-shard-3 : 250 users, 1274 messages
app-shard-4 : 251 users, 1249 messages
```
### Records on app-shard-4

!["Records on app-shard-4"](shard-data.png?raw=true)


# !!! Shard with caution
The demo uses a simple sharding mechanism e.g. id % number_of_shards, in real world system it can be anything from a simple md5 hash, to using some advanced hashing mechanism (Murmur hash, consistent hashing and others) or creating a hash on a combination of columns of the table.
Selection of shard key should be carefully done, so that records are distributed across shards evenly and the data you would need to fulfill most of the needs of your system would be available on a single shard.

Cross shard queries (queries which require accessing/checking records from multiple shards) are expensive and the code to do cross shard queries or asynchronously/parallely checking records on other shards can result in difficult to manage codebase and it also takes more processing and more time.

Companies which are using sharding on a large scale, usually have an additional Sharding layer between application code and database access. It might be building an interface/library as an internal tool or using available options such as ProxySQL, Vitess in case of MySQL.

Applications which uses database sharding also need to handle the scenarios where records might have to be migrated from one shard to a different shard, as the number of shards decreases or increases, resulting in records being assigned a different shard.

Join between two tables residing on two different shards can be very costly. Sharding must be applied only after thinking through all the scenarios.
Sharding is great but it comes with its own challenges and complexities.
