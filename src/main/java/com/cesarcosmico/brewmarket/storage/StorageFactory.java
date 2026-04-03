package com.cesarcosmico.brewmarket.storage;

import com.cesarcosmico.brewmarket.config.DatabaseConfig;
import com.cesarcosmico.brewmarket.config.DatabaseConfig.PooledSettings;
import com.cesarcosmico.brewmarket.config.DatabaseConfig.SQLiteSettings;
import com.cesarcosmico.brewmarket.storage.connection.ConnectionProvider;
import com.cesarcosmico.brewmarket.storage.connection.HikariConnectionProvider;
import com.cesarcosmico.brewmarket.storage.connection.SqliteConnectionProvider;
import com.cesarcosmico.brewmarket.storage.provider.JdbcSellHistoryService;
import com.cesarcosmico.brewmarket.storage.schema.MySqlSchemaManager;
import com.cesarcosmico.brewmarket.storage.schema.SchemaManager;
import com.cesarcosmico.brewmarket.storage.schema.SqliteSchemaManager;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class StorageFactory {

    private StorageFactory() {}

    public static SellHistoryService create(DatabaseConfig config, Path dataFolder, Logger logger) {
        return switch (config.getMethod()) {
            case "SQLite" -> {
                SQLiteSettings s = config.getSQLiteSettings();
                String table = resolveTable(s.tablePrefix());
                ConnectionProvider cp = new SqliteConnectionProvider(dataFolder.resolve(s.file() + ".db"));
                SchemaManager sm = new SqliteSchemaManager(table, logger);
                yield new JdbcSellHistoryService(cp, sm, table, logger, "SQLite");
            }
            case "MySQL" -> {
                PooledSettings s = config.getPooledSettings("MySQL");
                String table = resolveTable(s.tablePrefix());
                ConnectionProvider cp = new HikariConnectionProvider(
                        "jdbc:mysql://", "com.mysql.cj.jdbc.Driver", s, logger);
                SchemaManager sm = new MySqlSchemaManager(table, logger);
                yield new JdbcSellHistoryService(cp, sm, table, logger, "MySQL");
            }
            case "MariaDB" -> {
                PooledSettings s = config.getPooledSettings("MariaDB");
                String table = resolveTable(s.tablePrefix());
                ConnectionProvider cp = new HikariConnectionProvider(
                        "jdbc:mariadb://", "org.mariadb.jdbc.Driver", s, logger);
                SchemaManager sm = new MySqlSchemaManager(table, logger);
                yield new JdbcSellHistoryService(cp, sm, table, logger, "MariaDB");
            }
            default -> throw new IllegalArgumentException(
                    "Unknown data-storage-method '" + config.getMethod() + "'. Valid: SQLite, MySQL, MariaDB");
        };
    }

    private static String resolveTable(String prefix) {
        return (prefix == null || prefix.isBlank()) ? "sell_history" : prefix + "_sell_history";
    }
}
