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

