package com.cesarcosmico.brewmarket.storage.connection;

import com.cesarcosmico.brewmarket.config.DatabaseConfig.PooledSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class HikariConnectionProvider implements ConnectionProvider {

    private final String jdbcUrlPrefix;
    private final String driverClassName;
    private final PooledSettings settings;
    private final Logger logger;
    private HikariDataSource dataSource;

    public HikariConnectionProvider(String jdbcUrlPrefix,
                                    String driverClassName,
                                    PooledSettings settings,
                                    Logger logger) {
        this.jdbcUrlPrefix = jdbcUrlPrefix;
        this.driverClassName = driverClassName;
        this.settings = settings;
        this.logger = logger;
    }

    @Override
    public void initialize() throws SQLException {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName(driverClassName);
        cfg.setJdbcUrl(jdbcUrlPrefix + settings.host() + ":" + settings.port()
                + "/" + settings.database() + settings.connectionParameters());
        cfg.setPoolName("BrewMarket-Pool");
        cfg.setMaximumPoolSize(settings.poolSettings().maxPoolSize());
        cfg.setMinimumIdle(settings.poolSettings().minIdle());
        cfg.setMaxLifetime(settings.poolSettings().maxLifetime());
        cfg.setKeepaliveTime(settings.poolSettings().keepAliveTime());
        cfg.setConnectionTimeout(settings.poolSettings().timeout());
        cfg.setUsername(settings.user());
        cfg.setPassword(settings.password());

        try {
            dataSource = new HikariDataSource(cfg);
        } catch (Exception e) {
            throw new SQLException("Failed to create connection pool", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
