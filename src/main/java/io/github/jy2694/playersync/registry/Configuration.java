package io.github.jy2694.playersync.registry;

public class Configuration {
    private boolean velocityEnable;
    private String serverName;
    private int totalServerCount;
    private int requestTimeoutTicks;

    private String databaseType;
    private String databaseHost;
    private int databasePort;
    private String databaseUsername;
    private String databasePassword;

    public boolean isVelocityEnable(){
        return velocityEnable;
    }

    public String getServerName(){
        return serverName;
    }

    public int getTotalServerCount(){
        return totalServerCount;
    }

    public int getRequestTimeoutTicks(){
        return requestTimeoutTicks;
    }

    public String getDatabaseType(){
        return databaseType;
    }

    public String getDatabaseHost(){
        return databaseHost;
    }

    public int getDatabasePort(){
        return databasePort;
    }

    public String getDatabaseUsername(){
        return databaseUsername;
    }

    public String getDatabasePassword(){
        return databasePassword;
    }

    public void setVelocityEnable(boolean velocityEnable){
        this.velocityEnable = velocityEnable;
    }

    public void setServerName(String serverName){
        this.serverName = serverName;
    }

    public void setTotalServerCount(int totalServerCount){
        this.totalServerCount = totalServerCount;
    }

    public void setRequestTimeoutTicks(int requestTimeoutTicks){
        this.requestTimeoutTicks = requestTimeoutTicks;
    }

    public void setDatabaseType(String databaseType){
        this.databaseType = databaseType;
    }

    public void setDatabaseHost(String databaseHost){
        this.databaseHost = databaseHost;
    }

    public void setDatabasePort(int databasePort){
        this.databasePort = databasePort;
    }

    public void setDatabaseUsername(String databaseUsername){
        this.databaseUsername = databaseUsername;
    }

    public void setDatabasePassword(String databasePassword){
        this.databasePassword = databasePassword;
    }
}
