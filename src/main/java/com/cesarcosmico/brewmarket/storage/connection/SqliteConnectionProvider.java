package com.cesarcosmico.brewmarket.storage.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqliteConnectionProvider implements ConnectionProvider {

    private final Path dbPath;
    private Connection realConnection;
    private Connection nonClosingProxy;

    public SqliteConnectionProvider(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void initialize() throws SQLException {
        this.realConnection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (realConnection == null || realConnection.isClosed()) {
            realConnection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            nonClosingProxy = null;
        }
        if (nonClosingProxy == null) {
            nonClosingProxy = createNonClosingProxy(realConnection);
        }
        return nonClosingProxy;
    }

    @Override
    public void close() {
        if (realConnection != null) {
            try {
                realConnection.close();
            } catch (SQLException ignored) {
                // Best-effort close during shutdown \_(ツ)_/¯
            }
        }
    }

    private static Connection createNonClosingProxy(Connection delegate) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("close".equals(method.getName())) {
                return null;
            }
            return method.invoke(delegate, args);
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler
        );
    }
}
