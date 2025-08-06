package cc.hubailmn.utility.database.data;

import cc.hubailmn.utility.database.annotation.DataBaseTable;
import cc.hubailmn.utility.database.annotation.SQLColumn;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLSchemaGenerator {

    public static void createTable(Class<?> entityClass, Connection conn) throws SQLException {
        DataBaseTable tableAnno = entityClass.getAnnotation(DataBaseTable.class);
        if (tableAnno == null) throw new IllegalArgumentException("Missing @DataBaseTable");

        String tableName = tableAnno.name();
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        boolean isSQLite = conn.getMetaData().getDatabaseProductName().toLowerCase().contains("sqlite");

        List<String> indexStatements = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            SQLColumn column = field.getAnnotation(SQLColumn.class);
            if (column == null) continue;

            String columnName = column.name().isEmpty() ? field.getName() : column.name();
            String type = adaptType(column.type(), isSQLite);

            sql.append(columnName).append(" ").append(type);

            if (!column.defaultValue().isEmpty()) {
                sql.append(" DEFAULT ").append(quoteDefaultIfNeeded(column.defaultValue(), field.getType()));
            }

            if (column.primaryKey()) {
                sql.append(" PRIMARY KEY");
            }

            if (column.autoIncrement()) {
                if (isSQLite) {
                    if (!(column.primaryKey() && field.getType() == int.class)) {
                        throw new IllegalArgumentException("SQLite requires auto-increment fields to be INTEGER PRIMARY KEY");
                    }
                    sql.append(" AUTOINCREMENT");
                } else {
                    sql.append(" AUTO_INCREMENT");
                }
            }

            if (!column.nullable() || field.getType().isPrimitive() || column.primaryKey()) {
                sql.append(" NOT NULL");
            }

            if (!column.references().isEmpty()) {
                sql.append(" REFERENCES ").append(column.references());
            }

            sql.append(", ");

            if (column.index()) {
                indexStatements.add("CREATE INDEX IF NOT EXISTS idx_" + tableName + "_" + columnName +
                        " ON " + tableName + "(" + columnName + ")");
            } else if (column.uniqueIndex()) {
                indexStatements.add("CREATE UNIQUE INDEX IF NOT EXISTS uq_idx_" + tableName + "_" + columnName +
                        " ON " + tableName + "(" + columnName + ")");
            }
        }

        if (sql.length() > 2) sql.setLength(sql.length() - 2);
        sql.append(")");

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
            for (String indexSql : indexStatements) {
                stmt.executeUpdate(indexSql);
            }
        }
    }

    private static String adaptType(SQLType sqlType, boolean isSQLite) {
        return switch (sqlType) {
            case BOOLEAN -> isSQLite ? "INTEGER" : "TINYINT(1)";
            case TEXT -> isSQLite ? "TEXT" : "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            case VARCHAR -> "VARCHAR(255)";
            case INTEGER -> "INTEGER";
            case BIGINT -> "BIGINT";
            case DOUBLE -> "DOUBLE";
            case FLOAT -> "FLOAT";
            case DATE -> "DATE";
            case TIMESTAMP -> "TIMESTAMP";
            case BLOB -> "BLOB";
        };
    }

    private static String quoteDefaultIfNeeded(String value, Class<?> type) {
        if (value == null || value.isEmpty()) return "";
        if (type == String.class || type == char.class || type == Character.class) {
            if (!value.startsWith("'") && !value.endsWith("'")) {
                return "'" + value.replace("'", "''") + "'";
            }
        }
        return value;
    }

}
