package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.PlayerStats;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.RecipeTally;
import com.cesarcosmico.thebrewingmarket.util.MiniMessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStatsCache {

    private final SellHistoryService historyService;
    private final ConcurrentHashMap<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

    public PlayerStatsCache(SellHistoryService historyService) {
        this.historyService = historyService;
    }

    public void seedAsync(UUID uuid) {
        historyService.getPlayerStats(uuid).thenAccept(stats -> cache.put(uuid, stats));
    }

    public PlayerStats getStats(UUID uuid) {
        return cache.getOrDefault(uuid, PlayerStats.empty());
    }

    public RecipeTally recipeTally(UUID uuid, String recipeId) {
        PlayerStats stats = cache.get(uuid);
        if (stats == null || recipeId == null) return new RecipeTally(0L, 0.0);
        return stats.perRecipe().getOrDefault(recipeId.toLowerCase(), new RecipeTally(0L, 0.0));
    }

    public void recordSale(UUID uuid, SellService.DetailedSellResult result) {
        if (result.details().isEmpty()) return;

        cache.compute(uuid, (k, existing) -> {
            PlayerStats base = existing != null ? existing : PlayerStats.empty();
            double earned = base.lifetimeEarned();
            long brews = base.lifetimeBrews();
            Map<String, RecipeTally> perRecipe = new HashMap<>(base.perRecipe());
            String lastRecipe = base.lastRecipe();
            String lastDisplayName = base.lastDisplayName();
            double lastAmount = base.lastAmount();
            long lastAt = base.lastSoldAt();
            long now = System.currentTimeMillis();

            for (SellService.SoldBrewDetail d : result.details()) {
                double total = d.total();
                int qty = d.quantity();
                earned += total;
                brews += qty;

                String key = d.recipeId().toLowerCase();
                RecipeTally prev = perRecipe.getOrDefault(key, new RecipeTally(0L, 0.0));
                perRecipe.put(key, new RecipeTally(prev.quantity() + qty, prev.total() + total));

                if (now >= lastAt) {
                    lastRecipe = d.recipeId();
                    lastDisplayName = d.displayName() != null
                            ? MiniMessageUtil.toPlainText(d.displayName())
                            : d.recipeId();
                    lastAmount = total;
                    lastAt = now;
                }
            }

            return new PlayerStats(earned, brews, lastRecipe, lastDisplayName, lastAmount, lastAt, perRecipe);
        });
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }
}
