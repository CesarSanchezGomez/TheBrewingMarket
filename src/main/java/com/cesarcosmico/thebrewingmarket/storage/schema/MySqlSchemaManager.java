package com.cesarcosmico.thebrewingmarket.storage.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MySqlSchemaManager implements SchemaManager {

    private final String table;
    private final Logger logger;

    public MySqlSchemaManager(String table, Logger logger) {
        this.table = table;
        this.logger = logger;
    }

    @Override
    public void createSchema(Connection connection) throws SQLException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS `%s` (
                    id           BIGINT       NOT NULL AUTO_INCREMENT,
                    player_uuid  VARCHAR(36)  NOT NULL,
                    player_name  VARCHAR(32)  NOT NULL,
                    recipe_name  TEXT         NOT NULL,
                    display_name TEXT,
                    quality      DOUBLE       NOT NULL,
                    price_per    DOUBLE       NOT NULL,
                    quantity     INT          NOT NULL,
                    total        DOUBLE       NOT NULL,
                    sold_at      BIGINT       NOT NULL,
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;""".formatted(table);

        String createIndexPlayer = "CREATE INDEX idx_%s_player ON `%s`(player_uuid)"
                .formatted(table, table);
        String createIndexTime = "CREATE INDEX idx_%s_time ON `%s`(sold_at)"
                .formatted(table, table);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
            tryExecute(stmt, createIndexPlayer);
            tryExecute(stmt, createIndexTime);
        }
    }

    private void tryExecute(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.log(Level.FINE, "Index creation note: " + e.getMessage());
        }
    }
}
