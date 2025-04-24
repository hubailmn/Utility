package me.hubailmn.util.config.file;

import lombok.Getter;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.annotation.IgnoreFile;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;
import org.bukkit.configuration.file.FileConfiguration;

@LoadConfig(path = "DBConfig.yml")
@IgnoreFile(ifNoDatabase = true)
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

    public DBConfig() {
        String key = "database.SQLite.path";
        String rawPath = getConfig().getString(key);

        if (rawPath != null && rawPath.contains("%plugin_name%")) {
            String resolvedPath = rawPath.replace("%plugin_name%", BasePlugin.getPluginName());
            getConfig().set(key, resolvedPath);
        } else if (rawPath == null) {
            getConfig().set(key, BasePlugin.getPluginName() + ".db");
        }
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
