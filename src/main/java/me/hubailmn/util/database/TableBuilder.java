package me.hubailmn.util.database;

import lombok.Getter;
import me.hubailmn.util.database.annotation.DataBaseTable;
import me.hubailmn.util.interaction.CSend;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Getter
public abstract class TableBuilder {

    private final String name;
    private final Connection connection;

    public TableBuilder() {
        this(DataBaseConnection.getConnection());
    }

    public TableBuilder(Connection connection) {
        this.connection = connection;

        DataBaseTable annotation = this.getClass().getAnnotation(DataBaseTable.class);
        if (annotation == null) {
            throw new IllegalStateException("Subclasses of TableBuilder must be annotated with @DataBaseTable.");
        }

        this.name = annotation.name();

        try {
            createTable();
            init();
        } catch (SQLException e) {
            CSend.error("Failed to initialize table: " + name);
            CSend.error(e);
        }
    }

    protected abstract void createTable() throws SQLException;

    protected void init() throws SQLException {
    }

    protected void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
