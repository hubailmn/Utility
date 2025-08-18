package cc.hubailmn.utility.database.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class SQLiteConfig {
    private final String sqlitePath;

    public SQLiteConfig(FileConfiguration config) {
        this.sqlitePath = config.getString("database.SQLite.path");
        validate();
    }

    public SQLiteConfig(String sqlitePath) {
        this.sqlitePath = sqlitePath;
        validate();
    }

    private void validate() {
        if (sqlitePath == null || sqlitePath.isBlank()) {
            throw new IllegalArgumentException("SQLiteConfig: Path cannot be null or empty.");
        }
    }
}