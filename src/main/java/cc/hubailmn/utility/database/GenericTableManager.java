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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GenericTableManager<T> {

    private final Class<T> entityClass;
    private final Connection connection;
    private final String tableName;
    private final Map<String, Field> columnFieldMap = new LinkedHashMap<>();
    private final Map<String, Field> updatableFields = new LinkedHashMap<>();
    private final Field primaryKeyField;

    private final Map<Object, T> entityCache = new ConcurrentHashMap<>();
    private final long cacheExpiry = 30000;
    private final Map<Object, Long> cacheTimestamps = new ConcurrentHashMap<>();

    private PreparedStatement selectByPkStmt;
    private PreparedStatement insertStmt;
    private PreparedStatement updateStmt;
    private PreparedStatement deleteStmt;
    private boolean statementsReady = false;

    public GenericTableManager(Class<T> entityClass, Connection connection) {
        this(entityClass, connection, null);
    }

    public GenericTableManager(Class<T> entityClass, Connection connection, String serverPrefix) {
        this.entityClass = entityClass;
        this.connection = connection;

        DataBaseTable tableAnno = entityClass.getAnnotation(DataBaseTable.class);
        if (tableAnno == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @DataBaseTable");
        }

        String baseName = tableAnno.name();
        if (tableAnno.serverPrefix() && serverPrefix != null && !serverPrefix.isEmpty()) {
            this.tableName = serverPrefix + "_" + baseName;
        } else {
            this.tableName = baseName;
        }

        Field pkField = null;
        for (Field field : getAllFields(entityClass)) {
            SQLColumn col = field.getAnnotation(SQLColumn.class);
            if (col == null) continue;
            field.setAccessible(true);
            String colName = col.name().isEmpty() ? field.getName() : col.name();
            columnFieldMap.put(colName, field);

            if (col.updatable() && !col.primaryKey()) {
                updatableFields.put(colName, field);
            }

            if (col.primaryKey()) {
                pkField = field;
            }
        }
        if (pkField == null) {
            throw new IllegalArgumentException("Entity class must have one primary key field annotated with @SQLColumn(primaryKey=true)");
        }
        this.primaryKeyField = pkField;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private void prepareStatements() throws SQLException {
        String pkColumn = getColumnName(primaryKeyField);

        String selectSql = "SELECT * FROM \"" + tableName + "\" WHERE " + pkColumn + " = ?";
        selectByPkStmt = connection.prepareStatement(selectSql);

        String columns = String.join(", ", columnFieldMap.keySet());
        String placeholders = columnFieldMap.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        String insertSql = "INSERT INTO \"" + tableName + "\" (" + columns + ") VALUES (" + placeholders + ")";
        insertStmt = connection.prepareStatement(insertSql);

        if (!updatableFields.isEmpty()) {
            String updateFields = updatableFields.keySet().stream()
                    .map(col -> col + " = ?")
                    .collect(Collectors.joining(", "));
            String updateSql = "UPDATE \"" + tableName + "\" SET " + updateFields + " WHERE " + pkColumn + " = ?";
            updateStmt = connection.prepareStatement(updateSql);
        }

        String deleteSql = "DELETE FROM \"" + tableName + "\" WHERE " + pkColumn + " = ?";
        deleteStmt = connection.prepareStatement(deleteSql);
    }

    public void createTable() throws SQLException {
        SQLSchemaGenerator.createTable(entityClass, connection, tableName);
        if (!statementsReady) {
            prepareStatements();
            statementsReady = true;
        }
    }

    public void save(T entity) throws SQLException {
        validateEntity(entity);

        Object pkValue = getPrimaryKeyValue(entity);
        boolean isUpdate = pkValue != null && exists(pkValue);

        if (isUpdate) {
            update(entity);
        } else {
            insert(entity);
        }

        if (pkValue != null) {
            entityCache.put(pkValue, entity);
            cacheTimestamps.put(pkValue, System.currentTimeMillis());
        }
    }

    private void insert(T entity) throws SQLException {
        try {
            int idx = 1;
            for (Field field : columnFieldMap.values()) {
                Object value = field.get(entity);
                insertStmt.setObject(idx++, value);
            }
            int affected = insertStmt.executeUpdate();
            CSend.debug("Inserted entity in table '{}' - affected rows: {}", tableName, affected);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity fields for insertion", e);
        }
    }

    private void update(T entity) throws SQLException {
        if (updateStmt == null) return;

        try {
            int idx = 1;
            for (Field field : updatableFields.values()) {
                Object value = field.get(entity);
                updateStmt.setObject(idx++, value);
            }
            Object pkValue = getPrimaryKeyValue(entity);
            updateStmt.setObject(idx, pkValue);

            int affected = updateStmt.executeUpdate();
            CSend.debug("Updated entity in table '{}' - affected rows: {}", tableName, affected);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access entity fields for update", e);
        }
    }

    public void saveBatch(List<T> entities) throws SQLException {
        if (entities.isEmpty()) return;

        connection.setAutoCommit(false);
        try {
            for (T entity : entities) {
                save(entity);
            }
            connection.commit();
            CSend.debug("Saved batch of {} entities in table '{}'", entities.size(), tableName);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public Optional<T> load(Object primaryKeyValue) throws SQLException {
        if (primaryKeyValue == null) return Optional.empty();

        T cached = getCachedEntity(primaryKeyValue);
        if (cached != null) {
            return Optional.of(cached);
        }

        try {
            selectByPkStmt.setObject(1, primaryKeyValue);
            try (ResultSet rs = selectByPkStmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                T entity = createEntityFromResultSet(rs);

                entityCache.put(primaryKeyValue, entity);
                cacheTimestamps.put(primaryKeyValue, System.currentTimeMillis());

                return Optional.of(entity);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate or populate entity", e);
        }
    }

    public List<T> loadBatch(List<Object> primaryKeyValues) throws SQLException {
        List<T> results = new ArrayList<>();
        List<Object> toLoad = new ArrayList<>();

        for (Object pkValue : primaryKeyValues) {
            T cached = getCachedEntity(pkValue);
            if (cached != null) {
                results.add(cached);
            } else {
                toLoad.add(pkValue);
            }
        }

        if (!toLoad.isEmpty()) {
            String pkColumn = getColumnName(primaryKeyField);
            String placeholders = toLoad.stream().map(k -> "?").collect(Collectors.joining(", "));
            String sql = "SELECT * FROM \"" + tableName + "\" WHERE " + pkColumn + " IN (" + placeholders + ")";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < toLoad.size(); i++) {
                    stmt.setObject(i + 1, toLoad.get(i));
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        T entity = createEntityFromResultSet(rs);
                        Object pkValue = getPrimaryKeyValue(entity);
                        results.add(entity);

                        entityCache.put(pkValue, entity);
                        cacheTimestamps.put(pkValue, System.currentTimeMillis());
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create entities from result set", e);
            }
        }

        return results;
    }

    public boolean delete(Object primaryKeyValue) throws SQLException {
        if (primaryKeyValue == null) return false;

        try {
            deleteStmt.setObject(1, primaryKeyValue);
            int affected = deleteStmt.executeUpdate();

            if (affected > 0) {
                entityCache.remove(primaryKeyValue);
                cacheTimestamps.remove(primaryKeyValue);
            }

            return affected > 0;
        } catch (SQLException e) {
            CSend.error("Failed to delete entity with PK: " + primaryKeyValue, e);
            throw e;
        }
    }

    public List<T> findBy(String columnName, Object value) throws SQLException {
        String sql = "SELECT * FROM \"" + tableName + "\" WHERE " + columnName + " = ?";
        List<T> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T entity = createEntityFromResultSet(rs);
                    results.add(entity);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create entities from result set", e);
        }

        return results;
    }

    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"" + tableName + "\"";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    public boolean exists(Object primaryKeyValue) throws SQLException {
        if (primaryKeyValue == null) return false;

        if (getCachedEntity(primaryKeyValue) != null) {
            return true;
        }

        String pkColumn = getColumnName(primaryKeyField);
        String sql = "SELECT 1 FROM \"" + tableName + "\" WHERE " + pkColumn + " = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, primaryKeyValue);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void clearExpiredCache() {
        long now = System.currentTimeMillis();
        List<Object> keysToRemove = new ArrayList<>();

        for (Map.Entry<Object, Long> entry : cacheTimestamps.entrySet()) {
            if (now - entry.getValue() > cacheExpiry) {
                keysToRemove.add(entry.getKey());
            }
        }

        for (Object key : keysToRemove) {
            entityCache.remove(key);
            cacheTimestamps.remove(key);
        }
    }

    private T getCachedEntity(Object primaryKeyValue) {
        Long timestamp = cacheTimestamps.get(primaryKeyValue);
        if (timestamp == null || System.currentTimeMillis() - timestamp > cacheExpiry) {
            entityCache.remove(primaryKeyValue);
            cacheTimestamps.remove(primaryKeyValue);
            return null;
        }
        return entityCache.get(primaryKeyValue);
    }

    private T createEntityFromResultSet(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T entity = entityClass.getDeclaredConstructor().newInstance();
        for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
            String col = entry.getKey();
            Field field = entry.getValue();
            Object val = rs.getObject(col);

            if (val != null || !field.getType().isPrimitive()) {
                val = convertDatabaseValue(val, field.getType());
                field.set(entity, val);
            }
        }
        return entity;
    }

    /**
     * Converts database values to proper Java types
     */
    private Object convertDatabaseValue(Object dbValue, Class<?> targetType) {
        if (dbValue == null) {
            return null;
        }

        if (targetType == UUID.class && dbValue instanceof String) {
            return UUID.fromString((String) dbValue);
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (dbValue instanceof Number) {
                return ((Number) dbValue).intValue() != 0;
            }
            if (dbValue instanceof String) {
                String str = ((String) dbValue).toLowerCase();
                return "true".equals(str) || "1".equals(str);
            }
            if (dbValue instanceof Boolean) {
                return dbValue;
            }
        }

        if (targetType == int.class || targetType == Integer.class) {
            if (dbValue instanceof Number) {
                return ((Number) dbValue).intValue();
            }
        }

        if (targetType == long.class || targetType == Long.class) {
            if (dbValue instanceof Number) {
                return ((Number) dbValue).longValue();
            }
        }

        if (targetType == double.class || targetType == Double.class) {
            if (dbValue instanceof Number) {
                return ((Number) dbValue).doubleValue();
            }
        }

        if (targetType == float.class || targetType == Float.class) {
            if (dbValue instanceof Number) {
                return ((Number) dbValue).floatValue();
            }
        }

        return dbValue;
    }

    private Object getPrimaryKeyValue(T entity) {
        try {
            return primaryKeyField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get primary key value", e);
        }
    }

    private void validateEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        try {
            for (Field field : columnFieldMap.values()) {
                SQLColumn col = field.getAnnotation(SQLColumn.class);
                Object value = field.get(entity);

                if (!col.nullable() && value == null && !field.getType().isPrimitive()) {
                    throw new IllegalArgumentException("Field " + field.getName() + " cannot be null");
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to validate entity", e);
        }
    }

    private String getColumnName(Field field) {
        SQLColumn col = field.getAnnotation(SQLColumn.class);
        return (col != null && !col.name().isEmpty()) ? col.name() : field.getName();
    }

    public void close() {
        try {
            if (selectByPkStmt != null) selectByPkStmt.close();
            if (insertStmt != null) insertStmt.close();
            if (updateStmt != null) updateStmt.close();
            if (deleteStmt != null) deleteStmt.close();
        } catch (SQLException e) {
            CSend.error("Failed to close prepared statements", e);
        }
    }
}