package me.hubailmn.util.database;

import me.hubailmn.util.annotation.DataBaseTable;

import java.sql.Connection;

public abstract class DBTable {

    protected final String tableName;
    protected final Connection connection;

    public DBTable() {
        this.connection = DBConnection.getConnection();

        DataBaseTable annotation = this.getClass().getAnnotation(DataBaseTable.class);
        if (annotation == null) {
            throw new IllegalStateException("DBTable subclass must be annotated with @DataBaseTable.");
        }

        this.tableName = annotation.name();

        createTable();
    }

    protected abstract void createTable();
}