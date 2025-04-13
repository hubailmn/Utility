package me.hubailmn.util.config.file;

import lombok.Getter;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;
import org.bukkit.configuration.file.FileConfiguration;

@LoadConfig(path = "DBConfig.yml")
public class DBConfig extends ConfigBuilder {

    public String getModule() {
        return getConfig().getString("database.module");
    }

    public MySQLConfig getMySQLConfig() {
        return new MySQLConfig(getConfig());
    }

    public SQLiteConfig getSQLiteConfig() {
        return new SQLiteConfig(getConfig());
    }

    @Getter
    public static class MySQLConfig {
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
        }
    }

    @Getter
    public static class SQLiteConfig {
        private final String sqlitePath;

        public SQLiteConfig(FileConfiguration config) {
            this.sqlitePath = config.getString("database.SQLite.path");
        }
    }
}
