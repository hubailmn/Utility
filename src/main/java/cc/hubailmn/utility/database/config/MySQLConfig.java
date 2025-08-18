package cc.hubailmn.utility.database.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public final class MySQLConfig {

    private final String connectionString;
    private final String endPoint;
    private final String databaseName;
    private final String username;
    private final String password;

    public MySQLConfig(FileConfiguration config) {
        String path = "database.MySQL.";
        this.connectionString = config.getString(path + "connection-string");
        this.endPoint = config.getString(path + "endpoint");
        this.databaseName = config.getString(path + "database-name");
        this.username = config.getString(path + "username");
        this.password = config.getString(path + "password");

        validate();
    }

    public MySQLConfig(String endPoint, String databaseName, String username, String password) {
        this.endPoint = endPoint;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.connectionString = null;
        validate();
    }

    public MySQLConfig(String connectionString) {
        this.connectionString = connectionString;
        this.endPoint = null;
        this.databaseName = null;
        this.username = null;
        this.password = null;
        validate();
    }

    private void validate() {
        if ((connectionString == null || connectionString.isBlank())
                && (endPoint == null || databaseName == null || username == null || password == null)) {
            throw new IllegalArgumentException("MySQLConfig: Insufficient configuration provided.");
        }
    }
}