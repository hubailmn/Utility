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

    public static Connection createConnection(MySQLConfig config) throws SQLException {
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
        return dataSource.getConnection();
    }

    public static Connection createConnection(SQLiteConfig config) {
        File dbFile = new File(BasePlugin.getInstance().getDataFolder(), config.getSqlitePath());
        if (!dbFile.exists()) {
            if (!dbFile.getParentFile().mkdirs()) {
                CSend.error("Failed to create database folder.");
            }
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            CSend.error("Failed to connect to SQLite.");
            CSend.error(e);
            return null;
        }
    }
}