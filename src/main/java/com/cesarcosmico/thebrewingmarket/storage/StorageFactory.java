package com.cesarcosmico.thebrewingmarket.storage;

import com.cesarcosmico.thebrewingmarket.config.DatabaseConfig;
import com.cesarcosmico.thebrewingmarket.config.DatabaseConfig.PooledSettings;
import com.cesarcosmico.thebrewingmarket.config.DatabaseConfig.SQLiteSettings;
import com.cesarcosmico.thebrewingmarket.storage.connection.ConnectionProvider;
import com.cesarcosmico.thebrewingmarket.storage.connection.HikariConnectionProvider;
import com.cesarcosmico.thebrewingmarket.storage.connection.SqliteConnectionProvider;
import com.cesarcosmico.thebrewingmarket.storage.migration.Dialect;
import com.cesarcosmico.thebrewingmarket.storage.migration.MigrationManager;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class StorageFactory {

    private StorageFactory() {}

    public static SellHistoryService create(final DatabaseConfig config,
                                            final Path dataFolder,
                                            final Logger logger) {
        return switch (config.getMethod()) {
            case "SQLite" -> {
                final SQLiteSettings s = config.getSQLiteSettings();
                final String table = resolveTable(s.tablePrefix());
                final String versionTable = resolveVersionTable(s.tablePrefix());
                final ConnectionProvider cp = new SqliteConnectionProvider(dataFolder.resolve(s.file() + ".db"));
                final MigrationManager mm = new MigrationManager(Dialect.SQLITE, table, versionTable, logger);
                yield new JdbcSellHistoryService(cp, mm, table, logger, "SQLite");
            }
            case "MySQL"   -> createPooled(config, "MySQL",   "jdbc:mysql://",   "com.mysql.cj.jdbc.Driver",  logger);
            case "MariaDB" -> createPooled(config, "MariaDB", "jdbc:mariadb://", "org.mariadb.jdbc.Driver",    logger);
            default -> throw new IllegalArgumentException(
                    "Unknown data-storage-method '" + config.getMethod() + "'. Valid: SQLite, MySQL, MariaDB");
        };
    }

    private static SellHistoryService createPooled(final DatabaseConfig config,
                                                    final String section,
                                                    final String jdbcPrefix,
                                                    final String driverClass,
                                                    final Logger logger) {
        final PooledSettings s = config.getPooledSettings(section);
        final String table = resolveTable(s.tablePrefix());
        final String versionTable = resolveVersionTable(s.tablePrefix());
        final ConnectionProvider cp = new HikariConnectionProvider(jdbcPrefix, driverClass, s, logger);
        final MigrationManager mm = new MigrationManager(Dialect.MYSQL, table, versionTable, logger);
        return new JdbcSellHistoryService(cp, mm, table, logger, section);
    }

    private static String resolveTable(final String prefix) {
        return (prefix == null || prefix.isBlank()) ? "sell_history" : prefix + "_sell_history";
    }

    private static String resolveVersionTable(final String prefix) {
        return (prefix == null || prefix.isBlank()) ? "schema_version" : prefix + "_schema_version";
    }
}
