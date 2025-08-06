package cc.hubailmn.utility.database;

import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.database.annotation.SQLColumn;
import cc.hubailmn.utility.database.data.SQLSchemaGenerator;
import cc.hubailmn.utility.interaction.CSend;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GenericTableManager<T> {

    private final Class<T> entityClass;
    private final Connection connection;
    private final String tableName;
    private final Map<String, Field> columnFieldMap = new LinkedHashMap<>();
    private final Field primaryKeyField;

    public GenericTableManager(Class<T> entityClass, Connection connection) {
        this.entityClass = entityClass;
        this.connection = connection;

        DataBaseTable tableAnno = entityClass.getAnnotation(DataBaseTable.class);
        if (tableAnno == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @DataBaseTable");
        }
        this.tableName = tableAnno.name();

        Field pkField = null;
        for (Field field : entityClass.getDeclaredFields()) {
            SQLColumn col = field.getAnnotation(SQLColumn.class);
            if (col == null) continue;
            field.setAccessible(true);
            String colName = col.name().isEmpty() ? field.getName() : col.name();
            columnFieldMap.put(colName, field);
            if (col.primaryKey()) {
                pkField = field;
            }
        }
        if (pkField == null) {
            throw new IllegalArgumentException("Entity class must have one primary key field annotated with @SQLColumn(primaryKey=true)");
        }
        this.primaryKeyField = pkField;
    }

    public void createTable() throws SQLException {
        SQLSchemaGenerator.createTable(entityClass, connection);
    }

    public void save(T entity) throws SQLException {
        String columns = String.join(", ", columnFieldMap.keySet());
        String placeholders = columnFieldMap.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        String updates = columnFieldMap.entrySet().stream()
                .filter(e -> !e.getValue().equals(primaryKeyField))
                .map(e -> e.getKey() + " = VALUES(" + e.getKey() + ")")
                .collect(Collectors.joining(", "));

        boolean isSQLite = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");

        String sql;
        if (isSQLite) {
            sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ") " +
                    "ON CONFLICT(" + getColumnName(primaryKeyField) + ") DO UPDATE SET " +
                    columnFieldMap.entrySet().stream()
                            .filter(e -> !e.getValue().equals(primaryKeyField))
                            .map(e -> e.getKey() + " = excluded." + e.getKey())
                            .collect(Collectors.joining(", "));
        } else {
            sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ") " +
                    "ON DUPLICATE KEY UPDATE " + updates;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int idx = 1;
            for (Field field : columnFieldMap.values()) {
                Object value = field.get(entity);
                stmt.setObject(idx++, value);
            }
            int affected = stmt.executeUpdate();
            CSend.debug("Saved entity in table '{}' - affected rows: {}", tableName, affected);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity fields for saving", e);
        }
    }

    public Optional<T> load(Object primaryKeyValue) throws SQLException {
        String pkColumn = getColumnName(primaryKeyField);
        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKeyValue);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                T entity = entityClass.getDeclaredConstructor().newInstance();
                for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
                    String col = entry.getKey();
                    Field field = entry.getValue();
                    Object val = rs.getObject(col);
                    if (val != null || !field.getType().isPrimitive()) {
                        if (field.getType() == UUID.class && val instanceof String) {
                            val = UUID.fromString((String) val);
                        }
                        field.set(entity, val);
                    }
                }
                return Optional.of(entity);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate or populate entity", e);
        }
    }

    public boolean delete(Object primaryKeyValue) throws SQLException {
        String pkColumn = getColumnName(primaryKeyField);
        String sql = "DELETE FROM " + tableName + " WHERE " + pkColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKeyValue);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    private String getColumnName(Field field) {
        SQLColumn col = field.getAnnotation(SQLColumn.class);
        return (col != null && !col.name().isEmpty()) ? col.name() : field.getName();
    }
}
