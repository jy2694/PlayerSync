package io.github.jy2694.playersync.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.jy2694.playersync.PlayerSync;

public class RedisConnection extends AbstractDatabaseConnection {

    private final String password;
    private HikariDataSource dataSource;

    public RedisConnection(PlayerSync plugin, String host, int port, String password) {
        super(plugin, host, port);
        this.password = password;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("jdbc.RedisDriver");
        config.setJdbcUrl("jdbc:redis://" + host + ":" + port + "/0");
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
    }

    @Override
    public void disconnect() {
        dataSource.close();
    }

    @Override
    protected <T> T getData(UUID key, Class<T> clazz) {
        try {
            String redisKey = "playersync:" + clazz.getSimpleName() + ":" + key.toString();
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("GET " + redisKey);
            if (resultSet.next()) {
                String result = resultSet.getString("value");
                return deserialize(result, clazz);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected void saveData(Object data) {
        try {
            UUID key = PlayerSync.getAPI().getBinder().findKeyFromObject(data);
            String redisKey = "playersync:" + data.getClass().getSimpleName() + ":" + key.toString();
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SET " + redisKey + " " + serialize(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
