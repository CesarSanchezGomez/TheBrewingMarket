package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.PlayerAggregate;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.RecipeAggregate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

public final class MarketAnalyticsCache {

    public record Analytics(
            double totalToday,
            String topRecipe,
            String topRecipeName,
            long topRecipeQty,
            String topPlayer,
            double topPlayerAmount
    ) {
        public static Analytics empty() {
            return new Analytics(0.0, "", "", 0L, "", 0.0);
        }
    }

    private final SellHistoryService historyService;
    private volatile Analytics snapshot = Analytics.empty();
    private BukkitTask task;

    public MarketAnalyticsCache(SellHistoryService historyService) {
        this.historyService = historyService;
    }

    public void start(JavaPlugin plugin) {
        this.task = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, this::refresh, 0L, 20L * 60L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    // Snapshot read; refresh runs every 60s on the async executor.
    public Analytics getSnapshot() {
        return snapshot;
    }

    private void refresh() {
        long startOfDay = startOfLocalDayMillis();
        CompletableFuture<Double> totalFut = historyService.sumTotalAllSince(startOfDay);
        CompletableFuture<RecipeAggregate> recipeFut = historyService.getTopRecipeSince(startOfDay);
        CompletableFuture<PlayerAggregate> playerFut = historyService.getTopPlayerSince(startOfDay);

        CompletableFuture.allOf(totalFut, recipeFut, playerFut).whenComplete((v, ex) -> {
            if (ex != null) return;
            try {
                double total = totalFut.get();
                RecipeAggregate r = recipeFut.get();
                PlayerAggregate p = playerFut.get();
                snapshot = new Analytics(
                        total,
                        r != null ? r.recipeId() : "",
                        r != null ? r.displayName() : "",
                        r != null ? r.quantity() : 0L,
                        p != null ? p.playerName() : "",
                        p != null ? p.total() : 0.0
                );
            } catch (Exception ignored) {
            }
        });
    }

    private static long startOfLocalDayMillis() {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
