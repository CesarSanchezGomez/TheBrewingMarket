package com.cesarcosmico.thebrewingmarket.storage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SellHistoryService {

    record SellEntry(
            String recipeId,
            String displayName,
            double quality,
            double pricePerUnit,
            int quantity,
            double total
    ) {
    }

    record SellRecord(
            long id,
            UUID playerUuid,
            String playerName,
            String recipeId,
            String displayName,
            double quality,
            double pricePerUnit,
            int quantity,
            double total,
            long soldAt
    ) {
    }

    record RecipeTally(long quantity, double total) {
    }

    record PlayerStats(
            double lifetimeEarned,
            long lifetimeBrews,
            String lastRecipe,
            String lastDisplayName,
            double lastAmount,
            long lastSoldAt,
            Map<String, RecipeTally> perRecipe
    ) {
        public static PlayerStats empty() {
            return new PlayerStats(0.0, 0L, "", "", 0.0, 0L, Map.of());
        }
    }

    record RecipeAggregate(String recipeId, String displayName, long quantity) {
        public static RecipeAggregate empty() {
            return new RecipeAggregate("", "", 0L);
        }
    }

    record PlayerAggregate(String playerName, double total) {
        public static PlayerAggregate empty() {
            return new PlayerAggregate("", 0.0);
        }
    }

    CompletableFuture<Void> logEntries(UUID playerUuid, String playerName, List<SellEntry> entries);

    CompletableFuture<Optional<UUID>> findPlayerUuid(String playerName);

    CompletableFuture<List<SellRecord>> getHistory(UUID playerUuid, long since, int limit, int offset);

    CompletableFuture<Integer> countHistory(UUID playerUuid, long since);

    CompletableFuture<Double> sumTotalSince(UUID playerUuid, long sinceEpochMillis);

    CompletableFuture<PlayerStats> getPlayerStats(UUID playerUuid);

    CompletableFuture<Double> sumTotalAllSince(long sinceEpochMillis);

    CompletableFuture<RecipeAggregate> getTopRecipeSince(long sinceEpochMillis);

    CompletableFuture<PlayerAggregate> getTopPlayerSince(long sinceEpochMillis);

    void initialize() throws SQLException;

    void shutdown();
}
