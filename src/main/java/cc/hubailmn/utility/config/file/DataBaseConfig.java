package cc.hubailmn.utility.config.file;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.annotation.IgnoreFile;
import cc.hubailmn.utility.config.ConfigBuilder;
import cc.hubailmn.utility.config.annotation.LoadConfig;
import cc.hubailmn.utility.database.config.MySQLConfig;
import cc.hubailmn.utility.database.config.SQLiteConfig;

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

}
