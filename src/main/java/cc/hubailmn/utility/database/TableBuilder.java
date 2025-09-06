package cc.hubailmn.utility.database;

import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.plugin.CSend;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            CSend.error("Failed to initialize table: {}", name);
            CSend.error(e);
        }

    }

    protected abstract void createTable() throws SQLException;

    protected void init() throws SQLException {
    }

    @SneakyThrows
    protected void executeUpdate(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

}
