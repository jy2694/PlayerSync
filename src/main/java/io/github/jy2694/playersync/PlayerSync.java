package io.github.jy2694.playersync;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.jy2694.playersync.api.PlayerSyncAPI;
import io.github.jy2694.playersync.channel.VelocityChannel;
import io.github.jy2694.playersync.database.AbstractDatabaseConnection;
import io.github.jy2694.playersync.database.MariaDBConnection;
import io.github.jy2694.playersync.database.MySQLConnection;
import io.github.jy2694.playersync.database.RedisConnection;
import io.github.jy2694.playersync.event.PlayerSyncReloadedEvent;
import io.github.jy2694.playersync.registry.Configuration;

public class PlayerSync extends JavaPlugin {

    private static PlayerSyncAPI api;
    private Configuration configuration;
    private AbstractDatabaseConnection database;
    private VelocityChannel velocityChannel;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        readPluginConfig();
        if(!setUp()){
            getLogger().severe("Failed to set up PlayerSync");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        api = new PlayerSyncAPI(this);
    }

    @Override
    public void onDisable() {
        cleanUp();
    }

    private void cleanUp(){
        if(database != null){
            database.disconnect();
            database = null;
        }
        if(velocityChannel != null){
            velocityChannel = null;
        }
        configuration = null;
    }

    private void readPluginConfig(){
        reloadConfig();
        config = getConfig();
        Configuration configuration = new Configuration();
        configuration.setVelocityEnable(config.getBoolean("velocity.enable"));
        configuration.setServerName(config.getString("velocity.server-name"));
        configuration.setTotalServerCount(config.getInt("velocity.total-server-count"));
        configuration.setRequestTimeoutTicks(config.getInt("velocity.request-timeout-ticks"));
        configuration.setDatabaseType(config.getString("database.type"));
        if(configuration.getDatabaseType().equalsIgnoreCase("MYSQL")){
            configuration.setDatabaseHost(config.getString("database.mysql.host"));
            configuration.setDatabasePort(config.getInt("database.mysql.port"));
            configuration.setDatabaseUsername(config.getString("database.mysql.username"));
            configuration.setDatabasePassword(config.getString("database.mysql.password"));
        } else if(configuration.getDatabaseType().equalsIgnoreCase("MARIADB")){
            configuration.setDatabaseHost(config.getString("database.mariadb.host"));
            configuration.setDatabasePort(config.getInt("database.mariadb.port"));
            configuration.setDatabaseUsername(config.getString("database.mariadb.username"));
            configuration.setDatabasePassword(config.getString("database.mariadb.password"));
        } else if(configuration.getDatabaseType().equalsIgnoreCase("REDIS")){
            configuration.setDatabaseHost(config.getString("database.redis.host"));
            configuration.setDatabasePort(config.getInt("database.redis.port"));
            configuration.setDatabasePassword(config.getString("database.redis.password"));
        }
        this.configuration = configuration;
    }

    private boolean setUp(){
        if(configuration.isVelocityEnable()){
            velocityChannel = new VelocityChannel(this);
        }
        if(configuration.getDatabaseType().equalsIgnoreCase("MYSQL")){
            database = new MySQLConnection(this, 
            configuration.getDatabaseHost(), 
            configuration.getDatabasePort(),
            configuration.getDatabaseUsername(),
            configuration.getDatabasePassword());
        } else if(configuration.getDatabaseType().equalsIgnoreCase("MARIADB")){
            database = new MariaDBConnection(this, 
            configuration.getDatabaseHost(), 
            configuration.getDatabasePort(),
            configuration.getDatabaseUsername(),
            configuration.getDatabasePassword());
        } else if(configuration.getDatabaseType().equalsIgnoreCase("REDIS")){
            database = new RedisConnection(this, 
            configuration.getDatabaseHost(), 
            configuration.getDatabasePort(),
            configuration.getDatabasePassword());
        } else {
            getLogger().severe("Invalid database type: " + configuration.getDatabaseType());
            return false;
        }
        database.connect();
        if(api != null) Bukkit.getPluginManager().callEvent(new PlayerSyncReloadedEvent());
        return true;
    }

    public void onReload(){
        cleanUp();
        readPluginConfig();
        if(!setUp()){
            getLogger().severe("Failed to set up PlayerSync");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }
    
    public Configuration getConfiguration(){
        return configuration;
    }

    public AbstractDatabaseConnection getDatabase(){
        return database;
    }

    public VelocityChannel getVelocityChannel(){
        return velocityChannel;
    }

    public static PlayerSyncAPI getAPI() {
        return api;
    }
}
