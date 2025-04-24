package me.hubailmn.util.database;

import lombok.Getter;
import me.hubailmn.util.database.annotation.DataBaseTable;

import java.sql.Connection;

@Getter
public abstract class TableBuilder {

    protected final String name;
    protected final Connection connection;

    public TableBuilder() {
        this.connection = DataBaseConnection.getConnection();

        DataBaseTable annotation = this.getClass().getAnnotation(DataBaseTable.class);
        if (annotation == null) {
            throw new IllegalStateException("DBTable subclass must be annotated with @DataBaseTable.");
        }

        this.name = annotation.name();

        createTable();
    }

    protected abstract void createTable();
}