package com.cesarcosmico.brewmarket.storage.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider extends AutoCloseable {

    Connection getConnection() throws SQLException;

    void initialize() throws SQLException;

    @Override
    void close();
}
