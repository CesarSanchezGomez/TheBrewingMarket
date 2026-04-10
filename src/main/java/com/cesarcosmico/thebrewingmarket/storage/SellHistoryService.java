package com.cesarcosmico.thebrewingmarket.storage;

import java.sql.SQLException;
import java.util.List;
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

    CompletableFuture<Void> logEntries(UUID playerUuid, String playerName, List<SellEntry> entries);

    CompletableFuture<Optional<UUID>> findPlayerUuid(String playerName);

    CompletableFuture<List<SellRecord>> getHistory(UUID playerUuid, long since, int limit, int offset);

    CompletableFuture<Integer> countHistory(UUID playerUuid, long since);

    void initialize() throws SQLException;

    void shutdown();
}