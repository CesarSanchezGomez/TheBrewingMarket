package com.cesarcosmico.thebrewingmarket.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConfig {

    public static final int CURRENT_VERSION = 2;

    public record SQLiteSettings(String file, String tablePrefix) {}

    public record PoolSettings(
            int maxPoolSize,
            int minIdle,
            long maxLifetime,
            long keepAliveTime,
            long timeout
    ) {}

    public record PooledSettings(
            String host,
            int port,
            String user,
            String password,
            String database,
            String connectionParameters,
            PoolSettings poolSettings,
            String tablePrefix
    ) {}

    private final String method;
    private final SQLiteSettings sqliteSettings;
    private final PooledSettings mysqlSettings;
    private final PooledSettings mariadbSettings;

    public DatabaseConfig(FileConfiguration config) {
        this.method = config.getString("data-storage-method", "SQLite");

        ConfigurationSection sqlite = sectionIgnoreCase(config, "sqlite");
        if (sqlite != null) {
            this.sqliteSettings = new SQLiteSettings(
                    sqlite.getString("file", "TheBrewingMarket"),
                    sqlite.getString("table-prefix", "")
            );
        } else {
            this.sqliteSettings = new SQLiteSettings("TheBrewingMarket", "");
        }

        this.mysqlSettings  = readPooledSettings(config, "mysql");
        this.mariadbSettings = readPooledSettings(config, "mariadb");
    }

    private static PooledSettings readPooledSettings(FileConfiguration config, String section) {
        ConfigurationSection sec = sectionIgnoreCase(config, section);
        if (sec == null) return null;

        ConfigurationSection pool = sec.getConfigurationSection("pool-settings");
        PoolSettings poolSettings = pool == null
                ? new PoolSettings(10, 10, 180000, 60000, 20000)
                : new PoolSettings(
                pool.getInt("max-pool-size", 10),
                pool.getInt("min-idle", 10),
                pool.getLong("max-lifetime", 180000),
                pool.getLong("keep-alive-time", 60000),
                pool.getLong("time-out", 20000)
        );

        return new PooledSettings(
                sec.getString("host", "localhost"),
                sec.getInt("port", 3306),
                sec.getString("user", "root"),
                sec.getString("password", ""),
                sec.getString("database", "minecraft"),
                sec.getString("connection-parameters", ""),
                poolSettings,
                sec.getString("table-prefix", "")
        );
    }

    private static ConfigurationSection sectionIgnoreCase(ConfigurationSection parent, String name) {
        for (String key : parent.getKeys(false)) {
            if (key.equalsIgnoreCase(name)) {
                return parent.getConfigurationSection(key);
            }
        }
        return null;
    }

    public String getMethod() {
        return method;
    }

    public SQLiteSettings getSQLiteSettings() {
        return sqliteSettings;
    }

    public PooledSettings getPooledSettings(String section) {
        return switch (section.toLowerCase()) {
            case "mysql"   -> mysqlSettings;
            case "mariadb" -> mariadbSettings;
            default -> throw new IllegalArgumentException("Unknown section: " + section);
        };
    }
}