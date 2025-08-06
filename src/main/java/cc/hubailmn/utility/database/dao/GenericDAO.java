package cc.hubailmn.utility.database.dao;

import cc.hubailmn.utility.database.DataBaseConnection;
import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.database.annotation.SQLColumn;
import cc.hubailmn.utility.interaction.CSend;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GenericDAO<T> {

    private final Connection connection;
    private final boolean isSQLite;
    private final Class<T> type;
    private final Map<String, Field> columnFieldMap;
    private final List<Field> updatableFields;
    private final String tableName;
    private final String primaryKeyColumn;
    private final Field primaryKeyField;
    private final String insertSQL;

    public GenericDAO(Class<T> type) {
        this.connection = DataBaseConnection.getConnection();
        this.isSQLite = detectSQLite();
        this.type = type;

        DataBaseTable tableAnno = type.getAnnotation(DataBaseTable.class);
        if (tableAnno == null) {
            throw new IllegalArgumentException("Class " + type.getName() + " must be annotated with @DataBaseTable");
        }
        this.tableName = tableAnno.name();

        // Build maps
        this.columnFieldMap = buildColumnFieldMap();
        this.updatableFields = buildUpdatableFields();

        // Find primary key column and field
        Field pkField = null;
        String pkCol = null;
        for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
            SQLColumn col = entry.getValue().getAnnotation(SQLColumn.class);
            if (col.primaryKey()) {
                pkField = entry.getValue();
                pkCol = entry.getKey();
                break;
            }
        }
        if (pkField == null) throw new IllegalStateException("No primary key found in " + type.getName());
        this.primaryKeyField = pkField;
        this.primaryKeyColumn = pkCol;

        this.insertSQL = buildInsertSQL();

        CSend.debug("GenericDAO initialized for {} on {}", type.getSimpleName(), isSQLite ? "SQLite" : "MySQL");
    }

    private boolean detectSQLite() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            String dbName = meta.getDatabaseProductName();
            return dbName != null && dbName.toLowerCase().contains("sqlite");
        } catch (Exception e) {
            CSend.warn("Failed to detect database type, defaulting to MySQL", e);
            return false;
        }
    }

    private Map<String, Field> buildColumnFieldMap() {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field field : type.getDeclaredFields()) {
            SQLColumn column = field.getAnnotation(SQLColumn.class);
            if (column == null) continue;

            field.setAccessible(true);
            String columnName = column.name().isEmpty() ? field.getName() : column.name();
            map.put(columnName, field);
        }
        return Collections.unmodifiableMap(map);
    }

    private List<Field> buildUpdatableFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : columnFieldMap.values()) {
            SQLColumn column = field.getAnnotation(SQLColumn.class);
            if (column != null && column.updatable()) {
                fields.add(field);
            }
        }
        return Collections.unmodifiableList(fields);
    }

    private String buildInsertSQL() {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updates = new StringBuilder();

        for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
            String columnName = entry.getKey();
            Field field = entry.getValue();
            SQLColumn column = field.getAnnotation(SQLColumn.class);

            columns.append(columnName).append(", ");
            placeholders.append("?, ");

            if (column.updatable()) {
                if (updates.length() > 0) updates.append(", ");
                if (isSQLite) {
                    updates.append(columnName).append(" = excluded.").append(columnName);
                } else {
                    updates.append(columnName).append(" = VALUES(").append(columnName).append(")");
                }
            }
        }

        if (columns.length() > 2) columns.setLength(columns.length() - 2);
        if (placeholders.length() > 2) placeholders.setLength(placeholders.length() - 2);

        String sql;
        if (isSQLite) {
            sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
            if (updates.length() > 0) {
                sql += " ON CONFLICT(" + primaryKeyColumn + ") DO UPDATE SET " + updates;
            }
        } else {
            sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
            if (updates.length() > 0) {
                sql += " ON DUPLICATE KEY UPDATE " + updates;
            }
        }

        CSend.debug("Built SQL for {}: {}", type.getSimpleName(), sql);
        return sql;
    }

    public CompletableFuture<Void> saveAsync(T entity) {
        return CompletableFuture.runAsync(() -> save(entity));
    }

    public void save(T entity) {
        if (entity == null) {
            CSend.warn("Attempted to save null entity");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            setParameters(stmt, entity);
            int affected = stmt.executeUpdate();
            CSend.debug("Saved entity {} - affected rows: {}", entity, affected);
        } catch (SQLException | IllegalAccessException e) {
            CSend.error("Failed to save entity {}: {}", entity, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> saveBatchAsync(List<T> entities) {
        return CompletableFuture.runAsync(() -> saveBatch(entities));
    }

    public void saveBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            CSend.debug("No entities to save in batch");
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            int batchSize = 0;
            final int maxBatchSize = 1000;

            for (T entity : entities) {
                if (entity == null) continue;

                setParameters(stmt, entity);
                stmt.addBatch();
                batchSize++;

                if (batchSize >= maxBatchSize) {
                    int[] results = stmt.executeBatch();
                    CSend.debug("Executed batch of {} entities, affected rows: {}", batchSize, Arrays.stream(results).sum());
                    batchSize = 0;
                }
            }

            if (batchSize > 0) {
                int[] results = stmt.executeBatch();
                CSend.debug("Executed final batch of {} entities, affected rows: {}", batchSize, Arrays.stream(results).sum());
            }

        } catch (SQLException | IllegalAccessException e) {
            CSend.error("Failed to save batch: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement stmt, T entity) throws SQLException, IllegalAccessException {
        int index = 1;
        for (Field field : columnFieldMap.values()) {
            Object value = field.get(entity);
            stmt.setObject(index++, value);
        }
    }

    public CompletableFuture<T> findByIdAsync(Object primaryKey) {
        return CompletableFuture.supplyAsync(() -> findById(primaryKey));
    }

    public T findById(Object primaryKey) {
        if (primaryKey == null) {
            CSend.warn("Attempted to find entity by null primary key");
            return null;
        }

        String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKey);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    CSend.debug("Entity not found for primary key {}", primaryKey);
                    return null;
                }

                T entity = type.getDeclaredConstructor().newInstance();
                populateEntityFromResultSet(entity, rs);
                return entity;
            }
        } catch (Exception e) {
            CSend.error("Failed to find entity by primary key {}: {}", primaryKey, e.getMessage(), e);
            return null;
        }
    }

    private void populateEntityFromResultSet(T entity, ResultSet rs) throws SQLException, IllegalAccessException {
        for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
            String columnName = entry.getKey();
            Field field = entry.getValue();
            Class<?> type = field.getType();

            Object value = getValueFromResultSet(rs, columnName, type);
            if (value != null || !type.isPrimitive()) {
                field.set(entity, value);
            }
        }
    }

    private Object getValueFromResultSet(ResultSet rs, String columnName, Class<?> type) throws SQLException {
        try {
            rs.findColumn(columnName);
        } catch (SQLException e) {
            CSend.debug("Column {} not found in result set", columnName);
            return null;
        }

        if (type == UUID.class) {
            String uuidStr = rs.getString(columnName);
            return uuidStr != null ? UUID.fromString(uuidStr) : null;
        } else if (type == String.class) {
            return rs.getString(columnName);
        } else if (type == int.class || type == Integer.class) {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? (type.isPrimitive() ? 0 : null) : value;
        } else if (type == long.class || type == Long.class) {
            long value = rs.getLong(columnName);
            return rs.wasNull() ? (type.isPrimitive() ? 0L : null) : value;
        } else if (type == boolean.class || type == Boolean.class) {
            boolean value = rs.getBoolean(columnName);
            return rs.wasNull() ? (type.isPrimitive() ? false : null) : value;
        } else if (type == double.class || type == Double.class) {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? (type.isPrimitive() ? 0.0 : null) : value;
        } else if (type == float.class || type == Float.class) {
            float value = rs.getFloat(columnName);
            return rs.wasNull() ? (type.isPrimitive() ? 0.0f : null) : value;
        }

        CSend.warn("Unsupported field type: {} for column: {}", type.getSimpleName(), columnName);
        return null;
    }

    public CompletableFuture<Boolean> deleteByIdAsync(Object primaryKey) {
        return CompletableFuture.supplyAsync(() -> deleteById(primaryKey));
    }

    public boolean deleteById(Object primaryKey) {
        if (primaryKey == null) {
            CSend.warn("Attempted to delete entity with null primary key");
            return false;
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKey);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            CSend.error("Failed to delete entity by primary key {}: {}", primaryKey, e.getMessage(), e);
            return false;
        }
    }

    public CompletableFuture<Boolean> existsByIdAsync(Object primaryKey) {
        return CompletableFuture.supplyAsync(() -> existsById(primaryKey));
    }

    public boolean existsById(Object primaryKey) {
        if (primaryKey == null) return false;

        String sql = "SELECT 1 FROM " + tableName + " WHERE " + primaryKeyColumn + " = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKey);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            CSend.error("Failed to check existence of entity by primary key {}: {}", primaryKey, e.getMessage(), e);
            return false;
        }
    }
}
