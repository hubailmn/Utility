package cc.hubailmn.utility.database;

import cc.hubailmn.utility.BasePlugin;
import cc.hubailmn.utility.database.config.MySQLConfig;
import cc.hubailmn.utility.database.config.SQLiteConfig;
import cc.hubailmn.utility.interaction.CSend;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnectionFactory {

    private DatabaseConnectionFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Connection createConnection(MySQLConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        if (config.getConnectionString() != null) {
            hikariConfig.setJdbcUrl(config.getConnectionString());
        } else {
            hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getEndPoint() + "/" + config.getDatabaseName());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
        }
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setPoolName("MySQLPool");
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            CSend.error("Failed to connect to MySQL.");
            CSend.error(e);
            return null;
        }
    }

    public static Connection createConnection(SQLiteConfig config) {
        File dbFile = new File(BasePlugin.getInstance().getDataFolder(), config.getSqlitePath());

        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                CSend.error("Failed to create database folder: {}", parentDir.getAbsolutePath());
                return null;
            }
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            CSend.error("Failed to connect to SQLite: {}", dbFile.getAbsolutePath());
            CSend.error(e);
            return null;
        }
    }
}