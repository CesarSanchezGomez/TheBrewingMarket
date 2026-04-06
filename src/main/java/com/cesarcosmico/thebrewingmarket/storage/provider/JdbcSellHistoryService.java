package com.cesarcosmico.thebrewingmarket.storage.provider;

import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.storage.connection.ConnectionProvider;
import com.cesarcosmico.thebrewingmarket.storage.schema.SchemaManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JdbcSellHistoryService implements SellHistoryService {

    private final ConnectionProvider connectionProvider;
    private final SchemaManager schemaManager;
    private final Logger logger;
    private final String providerName;
    private final String table;
    private final ExecutorService executor;

    private final String insertSql;
    private final String selectHistorySql;
    private final String countHistorySql;

    public JdbcSellHistoryService(ConnectionProvider connectionProvider,
                                  SchemaManager schemaManager,
                                  String table,
                                  Logger logger,
                                  String providerName) {
        this.connectionProvider = connectionProvider;
        this.schemaManager = schemaManager;
        this.logger = logger;
        this.providerName = providerName;
        this.table = table;

        this.insertSql = """
                INSERT INTO `%s`
                (player_uuid, player_name, recipe_name, display_name, quality, price_per, quantity, total, sold_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".formatted(table);

        this.selectHistorySql = """
                SELECT id, player_uuid, player_name, recipe_name, display_name,
                       quality, price_per, quantity, total, sold_at
                FROM `%s`
                WHERE player_name = ? AND sold_at >= ?
                ORDER BY sold_at DESC, id DESC
                LIMIT ? OFFSET ?""".formatted(table);

        this.countHistorySql = "SELECT COUNT(*) FROM `%s` WHERE player_name = ? AND sold_at >= ?".formatted(table);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TheBrewingMarket-DB-" + providerName);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void initialize() throws SQLException {
        connectionProvider.initialize();
        try (Connection conn = connectionProvider.getConnection()) {
            schemaManager.createSchema(conn);
        }
        logger.info(providerName + " sell-history initialized (table: " + table + ").");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        connectionProvider.close();
    }

    @Override
    public CompletableFuture<Void> logEntries(UUID playerUuid, String playerName, List<SellEntry> entries) {
        if (entries.isEmpty()) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            long now = System.currentTimeMillis();
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (SellEntry entry : entries) {
                    ps.setString(1, playerUuid.toString());
                    ps.setString(2, playerName);
                    ps.setString(3, entry.recipeName());
                    ps.setString(4, entry.displayName());
                    ps.setDouble(5, entry.quality());
                    ps.setDouble(6, entry.pricePerUnit());
                    ps.setInt(7, entry.quantity());
                    ps.setDouble(8, entry.total());
                    ps.setLong(9, now);
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to log sell history", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<SellRecord>> getHistory(String playerName, long since, int limit, int offset) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellRecord> records = new ArrayList<>();
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(selectHistorySql)) {
                ps.setString(1, playerName);
                ps.setLong(2, since);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String displayName = rs.getString("display_name");
                        records.add(new SellRecord(
                                rs.getLong("id"),
                                UUID.fromString(rs.getString("player_uuid")),
                                rs.getString("player_name"),
                                rs.getString("recipe_name"),
                                displayName != null ? displayName : rs.getString("recipe_name"),
                                rs.getDouble("quality"),
                                rs.getDouble("price_per"),
                                rs.getInt("quantity"),
                                rs.getDouble("total"),
                                rs.getLong("sold_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to retrieve sell history by name", e);
            }
            return records;
        }, executor);
    }

    @Override
    public CompletableFuture<Integer> countHistory(String playerName, long since) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(countHistorySql)) {
                ps.setString(1, playerName);
                ps.setLong(2, since);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to count sell history by name", e);
            }
            return 0;
        }, executor);
    }

}
