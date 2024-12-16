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

