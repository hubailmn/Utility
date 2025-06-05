package cc.hubailmn.util.database;

import cc.hubailmn.util.BasePlugin;
import cc.hubailmn.util.config.ConfigUtil;
import cc.hubailmn.util.config.file.DBConfig;
import cc.hubailmn.util.interaction.CSend;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DataBaseConnection {

    @Getter
    private static DBConfig config = ConfigUtil.getConfig(DBConfig.class);

    @Setter
    private static Connection connection;

    private static HikariDataSource hikariDataSource;

    private DataBaseConnection() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (DataBaseConnection.class) {
                    if (connection == null || connection.isClosed()) {
                        initialize();
                    }
                }
            }
        } catch (SQLException e) {
            CSend.error("§cFailed to check or establish a database connection.");
            CSend.error(e);
        }
        return connection;
    }

    public static void initialize() {
        String module = config.getModule();
        if (module == null) {
            CSend.error("§cConfig value 'database.module' is missing.");
            throw new RuntimeException("Config value 'database.module' is missing.");
        }

        try {
            if (module.equalsIgnoreCase("mysql")) {
                connectToMySQL();
            } else {
                connectToSQLite();
            }
        } catch (Exception e) {
            CSend.error("§cDatabase initialization failed.");
            CSend.error(e);
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
            CSend.error(e);
        }

        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            CSend.info("§fHikariCP connection pool has been closed.");
        }
    }

    private static void connectToMySQL() throws SQLException {
        DBConfig.MySQLConfig mysql = config.getMySQLConfig();
        CSend.info("§fConnecting to §9MySQL with HikariCP...");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysql.getConnectionString());
        hikariConfig.setUsername(mysql.getUsername());
        hikariConfig.setPassword(mysql.getPassword());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setPoolName(BasePlugin.getPluginName() + "-Hikari");

        hikariDataSource = new HikariDataSource(hikariConfig);
        connection = hikariDataSource.getConnection();

        CSend.info("§aMySQL connection established.");
    }

    private static void connectToSQLite() {
        try {
            CSend.info("§fConnecting to §9SQLite...");
            String path = getSQLitePath();
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            CSend.info("§aSQLite connection established.");
        } catch (SQLException e) {
            CSend.error("§cFailed to connect to SQLite.");
            CSend.error(e);
        }
    }

    private static String getSQLitePath() {
        DBConfig.SQLiteConfig sqlite = config.getSQLiteConfig();
        String fileName = sqlite.getSqlitePath();

        File dataFolder = new File(BasePlugin.getInstance().getDataFolder(), "database");
        File dbFile = new File(dataFolder, fileName);

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            CSend.error("§cCould not create database folder.");
        }

        if (!dbFile.exists()) {
            try {
                if (!dbFile.createNewFile()) {
                    CSend.error("§cCould not create SQLite database file.");
                }
            } catch (IOException e) {
                CSend.error("§cError while creating SQLite database file.");
                CSend.error(e);
            }
        }

        return dbFile.getPath();
    }

    public static void reload() {
        CSend.info("§6Reloading database connection...");
        close();
        ConfigUtil.reload(DBConfig.class);
        config = ConfigUtil.getConfig(DBConfig.class);
        initialize();
    }

}
