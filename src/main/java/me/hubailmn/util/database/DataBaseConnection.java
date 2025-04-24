package me.hubailmn.util.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.hubailmn.util.BasePlugin;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.DBConfig;
import me.hubailmn.util.interaction.CSend;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {

    @Getter
    private static final DBConfig config = ConfigUtil.getConfig(DBConfig.class);

    @Setter
    private static Connection connection;

    private DataBaseConnection() {
        throw new IllegalStateException("Utility class");
    }

    @SneakyThrows
    public static Connection getConnection() {
        if (connection == null || connection.isClosed()) {
            synchronized (DataBaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    initialize();
                }
            }
        }
        return connection;
    }


    public static void initialize() {
        String module = config.getModule();
        if (module == null) {
            throw new RuntimeException("Config value 'database.module' is missing.");
        }

        if (module.equalsIgnoreCase("mysql")) {
            connectToMySQL();
        } else {
            connectToSQLite();
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                CSend.info("§fDatabase connection has been closed.");
            }
        } catch (SQLException e) {
            CSend.error("§cError while closing the database connection.");
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static void connectToMySQL() {
        DBConfig.MySQLConfig mysql = config.getMySQLConfig();
        CSend.info("§fConnecting to §9MySQL with HikariCP...");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysql.getConnectionString());
        hikariConfig.setUsername(mysql.getUsername());
        hikariConfig.setPassword(mysql.getPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setPoolName(BasePlugin.getPluginName() + "-Pool");

        @SuppressWarnings("resource")
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        setConnection(dataSource.getConnection());
    }


    @SneakyThrows
    private static void connectToSQLite() {
        CSend.info("§fConnecting to §9SQLite...");
        String path = getSQLitePath();
        setConnection(DriverManager.getConnection("jdbc:sqlite:" + path));
    }

    private static String getSQLitePath() {
        DBConfig.SQLiteConfig sqlite = config.getSQLiteConfig();
        String fileName = sqlite.getSqlitePath();

        File dataFolder = new File(BasePlugin.getInstance().getDataFolder(), "database");
        File dbFile = new File(dataFolder, fileName);

        if (!dbFile.exists()) {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                throw new RuntimeException("Could not create database folder.");
            }

            try {
                if (!dbFile.createNewFile()) {
                    throw new IOException("Could not create SQLite database file.");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to create the database file", e);
            }
        }

        return dbFile.getPath();
    }
}
