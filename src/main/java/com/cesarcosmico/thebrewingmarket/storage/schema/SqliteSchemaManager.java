package com.cesarcosmico.thebrewingmarket.storage.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public final class SqliteSchemaManager implements SchemaManager {

    private final String table;
    private final Logger logger;

    public SqliteSchemaManager(String table, Logger logger) {
        this.table = table;
        this.logger = logger;
    }

    @Override
    public void createSchema(Connection connection) throws SQLException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid  TEXT    NOT NULL,
                    player_name  TEXT    NOT NULL,
                    recipe_name  TEXT    NOT NULL,
                    display_name TEXT,
                    quality      REAL    NOT NULL,
                    price_per    REAL    NOT NULL,
                    quantity     INTEGER NOT NULL,
                    total        REAL    NOT NULL,
                    sold_at      INTEGER NOT NULL
                )""".formatted(table);

        String createIndexTime = "CREATE INDEX IF NOT EXISTS idx_%s_time ON %s(sold_at)"
                .formatted(table, table);
        String createIndexPlayerName = "CREATE INDEX IF NOT EXISTS idx_%s_player_name ON %s(player_name)"
                .formatted(table, table);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
            stmt.execute(createIndexTime);
            stmt.execute(createIndexPlayerName);
        }
    }

}
