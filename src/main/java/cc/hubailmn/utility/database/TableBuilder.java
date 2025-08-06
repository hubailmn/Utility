package cc.hubailmn.utility.database;

import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.database.data.SQLSchemaGenerator;
import cc.hubailmn.utility.interaction.CSend;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Getter
public abstract class TableBuilder<T> {

    private final String name;
    private final Connection connection;
    private final Class<T> type;

    public TableBuilder() {
        this(DataBaseConnection.getConnection());
    }

    public TableBuilder(Connection connection) {
        this.type = (Class<T>) resolveGenericType();
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

        try {
            createTableFromType();
        } catch (SQLException e) {
            CSend.error("Failed to auto-generate SQL schema for table '{}' (class: {}).", name, type.getName());
            CSend.error("Check that all fields in {} are correctly annotated with @SQLColumn and avoid unsupported types.", type.getSimpleName());
            CSend.error(e);
        }

    }

    private Type resolveGenericType() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private void createTableFromType() throws SQLException {
        SQLSchemaGenerator.createTable(type, getConnection());
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
