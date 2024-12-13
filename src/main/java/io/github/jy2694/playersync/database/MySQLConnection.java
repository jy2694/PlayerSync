package io.github.jy2694.playersync.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.jy2694.playersync.PlayerSync;

public class MySQLConnection extends AbstractDatabaseConnection {

    private final String user;
    private final String password;
    private final String database;

    private HikariDataSource dataSource;

    public MySQLConnection(PlayerSync plugin, String host, int port, String user, String password) {
        super(plugin, host, port);
        this.user = user;
        this.password = password;
        this.database = "playersync";
    }

    @Override
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, user, password);
            connection.createStatement().execute("CREATE DATABASE IF NOT EXISTS " + database);
            connection.close();
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            dataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected <T> T getData(UUID key, Class<T> clazz) {
        try {
            String tableName = clazz.getSimpleName();
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName + " (uuid VARCHAR(36), data TEXT)");
            connection.close();
            PreparedStatement statement = connection.prepareStatement("SELECT data FROM playersync WHERE uuid = ?");
            statement.setString(1, key.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return deserialize(result.getString("data"), clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void saveData(Object data) {
        try {
            UUID key = PlayerSync.getAPI().getBinder().findKeyFromObject(data);
            String tableName = data.getClass().getSimpleName();
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName + " (uuid VARCHAR(36), data TEXT)");
            connection.close();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO playersync (uuid, data) VALUES (?, ?)");
            statement.setString(1, key.toString());
            statement.setString(2, serialize(data));
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
