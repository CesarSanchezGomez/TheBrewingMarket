package com.cesarcosmico.thebrewingmarket.storage.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MigrationManager {

    private static final int CURRENT_VERSION = 1;

    private final Dialect dialect;
    private final String table;
    private final String versionTable;
    private final Logger logger;

    public MigrationManager(final Dialect dialect,
                            final String table,
                            final String versionTable,
                            final Logger logger) {
        this.dialect = dialect;
        this.table = table;
        this.versionTable = versionTable;
        this.logger = logger;
    }

    public void migrate(final Connection conn) throws SQLException {
        createVersionTable(conn);
        final int version = getVersion(conn);

        if (version >= CURRENT_VERSION) {
            return;
        }

        if (version == 0 && !tableExists(conn)) {
            logger.info("No existing schema found. Creating fresh schema v" + CURRENT_VERSION + ".");
            createFreshSchemaV1(conn);
            setVersion(conn, CURRENT_VERSION);
            return;
        }

        for (int v = version + 1; v <= CURRENT_VERSION; v++) {
            logger.info("Applying schema migration v" + (v - 1) + " -> v" + v + "...");
            applyMigration(conn, v);
            setVersion(conn, v);
            logger.info("Migration to v" + v + " complete.");
        }
    }

    // ── Migration steps ──────────────────────────────────────────

    private void applyMigration(final Connection conn, final int targetVersion) throws SQLException {
        switch (targetVersion) {
            case 1 -> migrateV0ToV1(conn);
            default -> throw new IllegalStateException("Unknown migration target: v" + targetVersion);
        }
    }

    /**
     * v0 → v1: rename recipe_name → recipe_id; replace separate indexes
     * with a single composite index on (player_uuid, sold_at DESC).
     */
    private void migrateV0ToV1(final Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (dialect == Dialect.SQLITE) {
                stmt.execute("ALTER TABLE `" + table + "` RENAME COLUMN recipe_name TO recipe_id");
                stmt.execute("DROP INDEX IF EXISTS idx_" + table + "_time");
                stmt.execute("DROP INDEX IF EXISTS idx_" + table + "_player_name");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_" + table + "_player_time"
                        + " ON `" + table + "`(player_uuid, sold_at DESC)");
            } else {
                stmt.execute("ALTER TABLE `" + table + "` RENAME COLUMN recipe_name TO recipe_id");
                tryExecute(stmt, "DROP INDEX idx_" + table + "_time ON `" + table + "`");
                tryExecute(stmt, "DROP INDEX idx_" + table + "_player_name ON `" + table + "`");
                tryExecute(stmt, "CREATE INDEX idx_" + table + "_player_time"
                        + " ON `" + table + "`(player_uuid, sold_at DESC)");
            }
        }
    }

    // ── Fresh schema (v1) ────────────────────────────────────────

    private void createFreshSchemaV1(final Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (dialect == Dialect.SQLITE) {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS `%s` (
                            id           INTEGER PRIMARY KEY AUTOINCREMENT,
                            player_uuid  TEXT    NOT NULL,
                            player_name  TEXT    NOT NULL,
                            recipe_id    TEXT    NOT NULL,
                            display_name TEXT,
                            quality      REAL    NOT NULL,
                            price_per    REAL    NOT NULL,
                            quantity     INTEGER NOT NULL,
                            total        REAL    NOT NULL,
                            sold_at      INTEGER NOT NULL
                        )""".formatted(table));
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_" + table + "_player_time"
                        + " ON `" + table + "`(player_uuid, sold_at DESC)");
            } else {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS `%s` (
                            id           BIGINT      NOT NULL AUTO_INCREMENT,
                            player_uuid  VARCHAR(36) NOT NULL,
                            player_name  VARCHAR(32) NOT NULL,
                            recipe_id    TEXT        NOT NULL,
                            display_name TEXT,
                            quality      DOUBLE      NOT NULL,
                            price_per    DOUBLE      NOT NULL,
                            quantity     INT         NOT NULL,
                            total        DOUBLE      NOT NULL,
                            sold_at      BIGINT      NOT NULL,
                            PRIMARY KEY (id)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""".formatted(table));
                tryExecute(stmt, "CREATE INDEX idx_" + table + "_player_time"
                        + " ON `" + table + "`(player_uuid, sold_at DESC)");
            }
        }
    }

    // ── Version table helpers ────────────────────────────────────

    private void createVersionTable(final Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (dialect == Dialect.SQLITE) {
                stmt.execute("CREATE TABLE IF NOT EXISTS `" + versionTable + "` (version INTEGER NOT NULL)");
            } else {
                stmt.execute("CREATE TABLE IF NOT EXISTS `" + versionTable
                        + "` (version INT NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            }
        }
    }

    private int getVersion(final Connection conn) throws SQLException {
        final String sql = "SELECT version FROM `" + versionTable + "` LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setVersion(final Connection conn, final int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM `" + versionTable + "`");
        }
        final String sql = "INSERT INTO `" + versionTable + "` (version) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, version);
            ps.executeUpdate();
        }
    }

    // ── Table existence check ────────────────────────────────────

    private boolean tableExists(final Connection conn) throws SQLException {
        if (dialect == Dialect.SQLITE) {
            final String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } else {
            final String sql = "SELECT COUNT(*) FROM information_schema.TABLES"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        }
    }

    // ── Utilities ────────────────────────────────────────────────

    private void tryExecute(final Statement stmt, final String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.log(Level.FINE, "Non-fatal during migration: " + e.getMessage());
        }
    }
}
