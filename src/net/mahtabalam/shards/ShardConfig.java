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
