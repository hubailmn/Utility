package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@LoadConfig(path = "database/DataBaseConfig.yml")
@IgnoreFile(database = true)
public class DataBaseConfig extends ConfigBuilder {

    public DataBaseConfig() {
        super();
        String key = "database.SQLite.path";
        String rawPath = getConfig().getString(key);

        if (rawPath != null && rawPath.contains("%plugin_name%")) {
            String resolvedPath = rawPath.replace("%plugin_name%", BasePlugin.getInstance().getPluginName());
            getConfig().set(key, resolvedPath);
        } else if (rawPath == null) {
            getConfig().set(key, BasePlugin.getInstance().getPluginName() + ".db");
        }

        save();
    }

    @Override
    public void reloadCache() {
    }

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
