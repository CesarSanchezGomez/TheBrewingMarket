package com.cesarcosmico.thebrewingmarket.storage;

import com.cesarcosmico.thebrewingmarket.storage.connection.ConnectionProvider;
import com.cesarcosmico.thebrewingmarket.storage.migration.MigrationManager;
import com.cesarcosmico.thebrewingmarket.util.MiniMessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JdbcSellHistoryService implements SellHistoryService {

    private final ConnectionProvider connectionProvider;
    private final MigrationManager migrationManager;
    private final Logger logger;
    private final String providerName;
    private final String table;
    private final ExecutorService executor;

    private final String insertSql;
    private final String findPlayerUuidSql;
    private final String selectHistorySql;
    private final String countHistorySql;
    private final String sumTotalSinceSql;
    private final String playerRecipeAggregateSql;
    private final String playerLastSaleSql;
    private final String sumTotalAllSinceSql;
    private final String topRecipeSinceSql;
    private final String topPlayerSinceSql;

    public JdbcSellHistoryService(final ConnectionProvider connectionProvider,
                                  final MigrationManager migrationManager,
                                  final String table,
                                  final Logger logger,
                                  final String providerName) {
        this.connectionProvider = connectionProvider;
        this.migrationManager = migrationManager;
        this.logger = logger;
        this.providerName = providerName;
        this.table = table;

        this.insertSql = """
                INSERT INTO `%s`
                (player_uuid, player_name, recipe_id, display_name, quality, price_per, quantity, total, sold_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".formatted(table);

        this.findPlayerUuidSql =
                "SELECT player_uuid FROM `%s` WHERE player_name = ? LIMIT 1".formatted(table);

        this.selectHistorySql = """
                SELECT id, player_uuid, player_name, recipe_id, display_name,
                       quality, price_per, quantity, total, sold_at
                FROM `%s`
                WHERE player_uuid = ? AND sold_at >= ?
                ORDER BY sold_at DESC, id DESC
                LIMIT ? OFFSET ?""".formatted(table);

        this.countHistorySql =
                "SELECT COUNT(*) FROM `%s` WHERE player_uuid = ? AND sold_at >= ?".formatted(table);

        this.sumTotalSinceSql =
                "SELECT COALESCE(SUM(total), 0) FROM `%s` WHERE player_uuid = ? AND sold_at >= ?".formatted(table);

        this.playerRecipeAggregateSql = """
                SELECT recipe_id, SUM(total) AS total_sum, SUM(quantity) AS qty_sum, MAX(sold_at) AS last_at
                FROM `%s`
                WHERE player_uuid = ?
                GROUP BY recipe_id""".formatted(table);

        this.playerLastSaleSql = """
                SELECT recipe_id, display_name, total, sold_at
                FROM `%s`
                WHERE player_uuid = ?
                ORDER BY sold_at DESC, id DESC
                LIMIT 1""".formatted(table);

        this.sumTotalAllSinceSql =
                "SELECT COALESCE(SUM(total), 0) FROM `%s` WHERE sold_at >= ?".formatted(table);

        this.topRecipeSinceSql = """
                SELECT s1.recipe_id,
                       SUM(s1.quantity) AS qty,
                       (SELECT s2.display_name FROM `%s` s2
                        WHERE s2.recipe_id = s1.recipe_id AND s2.sold_at >= ?
                        ORDER BY s2.sold_at DESC, s2.id DESC LIMIT 1) AS display_name
                FROM `%s` s1
                WHERE s1.sold_at >= ?
                GROUP BY s1.recipe_id
                ORDER BY qty DESC, s1.recipe_id ASC
                LIMIT 1""".formatted(table, table);

        this.topPlayerSinceSql = """
                SELECT player_name, SUM(total) AS earned
                FROM `%s`
                WHERE sold_at >= ?
                GROUP BY player_uuid, player_name
                ORDER BY earned DESC, player_name ASC
                LIMIT 1""".formatted(table);

        this.executor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r, "TheBrewingMarket-DB-" + providerName);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void initialize() throws SQLException {
        connectionProvider.initialize();
        try (Connection conn = connectionProvider.getConnection()) {
            migrationManager.migrate(conn);
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
    public CompletableFuture<Void> logEntries(final UUID playerUuid,
                                              final String playerName,
                                              final List<SellEntry> entries) {
        if (entries.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            final long now = System.currentTimeMillis();
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (final SellEntry entry : entries) {
                    ps.setString(1, playerUuid.toString());
                    ps.setString(2, playerName);
                    ps.setString(3, entry.recipeId());
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
    public CompletableFuture<Optional<UUID>> findPlayerUuid(final String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(findPlayerUuidSql)) {
                ps.setString(1, playerName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(UUID.fromString(rs.getString("player_uuid")));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to find player UUID for name: " + playerName, e);
            }
            return Optional.empty();
        }, executor);
    }

    @Override
    public CompletableFuture<List<SellRecord>> getHistory(final UUID playerUuid,
                                                          final long since,
                                                          final int limit,
                                                          final int offset) {
        return CompletableFuture.supplyAsync(() -> {
            final List<SellRecord> records = new ArrayList<>();
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(selectHistorySql)) {
                ps.setString(1, playerUuid.toString());
                ps.setLong(2, since);
                ps.setInt(3, limit);
                ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final String displayName = rs.getString("display_name");
                        records.add(new SellRecord(
                                rs.getLong("id"),
                                UUID.fromString(rs.getString("player_uuid")),
                                rs.getString("player_name"),
                                rs.getString("recipe_id"),
                                displayName != null ? displayName : rs.getString("recipe_id"),
                                rs.getDouble("quality"),
                                rs.getDouble("price_per"),
                                rs.getInt("quantity"),
                                rs.getDouble("total"),
                                rs.getLong("sold_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to retrieve sell history", e);
            }
            return records;
        }, executor);
    }

    @Override
    public CompletableFuture<Integer> countHistory(final UUID playerUuid, final long since) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(countHistorySql)) {
                ps.setString(1, playerUuid.toString());
                ps.setLong(2, since);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to count sell history", e);
            }
            return 0;
        }, executor);
    }

    @Override
    public CompletableFuture<Double> sumTotalSince(final UUID playerUuid, final long sinceEpochMillis) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sumTotalSinceSql)) {
                ps.setString(1, playerUuid.toString());
                ps.setLong(2, sinceEpochMillis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to sum earnings", e);
            }
            return 0.0;
        }, executor);
    }

    @Override
    public CompletableFuture<PlayerStats> getPlayerStats(final UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            double lifetimeEarned = 0.0;
            long lifetimeBrews = 0L;
            Map<String, RecipeTally> perRecipe = new HashMap<>();
            String lastRecipe = "";
            String lastDisplayName = "";
            double lastAmount = 0.0;
            long lastSoldAt = 0L;

            try (Connection conn = connectionProvider.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement(playerRecipeAggregateSql)) {
                    ps.setString(1, playerUuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String recipeId = rs.getString("recipe_id");
                            double totalSum = rs.getDouble("total_sum");
                            long qtySum = rs.getLong("qty_sum");
                            lifetimeEarned += totalSum;
                            lifetimeBrews += qtySum;
                            if (recipeId != null) {
                                perRecipe.put(recipeId.toLowerCase(), new RecipeTally(qtySum, totalSum));
                            }
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(playerLastSaleSql)) {
                    ps.setString(1, playerUuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            lastRecipe = rs.getString("recipe_id");
                            if (lastRecipe == null) lastRecipe = "";
                            String rawDisplay = rs.getString("display_name");
                            if (rawDisplay != null && !rawDisplay.isEmpty()) {
                                lastDisplayName = MiniMessageUtil.toPlainText(rawDisplay);
                            } else {
                                lastDisplayName = lastRecipe;
                            }
                            lastAmount = rs.getDouble("total");
                            lastSoldAt = rs.getLong("sold_at");
                        }
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load player stats", e);
                return PlayerStats.empty();
            }

            return new PlayerStats(lifetimeEarned, lifetimeBrews,
                    lastRecipe, lastDisplayName, lastAmount, lastSoldAt, perRecipe);
        }, executor);
    }

    @Override
    public CompletableFuture<Double> sumTotalAllSince(final long sinceEpochMillis) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sumTotalAllSinceSql)) {
                ps.setLong(1, sinceEpochMillis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to sum global earnings", e);
            }
            return 0.0;
        }, executor);
    }

    @Override
    public CompletableFuture<RecipeAggregate> getTopRecipeSince(final long sinceEpochMillis) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(topRecipeSinceSql)) {
                ps.setLong(1, sinceEpochMillis);
                ps.setLong(2, sinceEpochMillis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String recipeId = rs.getString("recipe_id");
                        if (recipeId == null) recipeId = "";
                        String rawDisplay = rs.getString("display_name");
                        String displayName = (rawDisplay != null && !rawDisplay.isEmpty())
                                ? MiniMessageUtil.toPlainText(rawDisplay)
                                : recipeId;
                        long qty = rs.getLong("qty");
                        return new RecipeAggregate(recipeId, displayName, qty);
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to fetch top recipe", e);
            }
            return RecipeAggregate.empty();
        }, executor);
    }

    @Override
    public CompletableFuture<PlayerAggregate> getTopPlayerSince(final long sinceEpochMillis) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connectionProvider.getConnection();
                 PreparedStatement ps = conn.prepareStatement(topPlayerSinceSql)) {
                ps.setLong(1, sinceEpochMillis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("player_name");
                        double earned = rs.getDouble("earned");
                        return new PlayerAggregate(name != null ? name : "", earned);
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to fetch top player", e);
            }
            return PlayerAggregate.empty();
        }, executor);
    }
}
