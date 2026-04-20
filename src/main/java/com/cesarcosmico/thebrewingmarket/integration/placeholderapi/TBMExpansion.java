package com.cesarcosmico.thebrewingmarket.integration.placeholderapi;

import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig.LimitationConfig;
import com.cesarcosmico.thebrewingmarket.service.DailyEarningsTracker;
import com.cesarcosmico.thebrewingmarket.service.MarketAnalyticsCache;
import com.cesarcosmico.thebrewingmarket.service.MarketAnalyticsCache.Analytics;
import com.cesarcosmico.thebrewingmarket.service.PlayerStatsCache;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.PlayerStats;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService.RecipeTally;
import com.cesarcosmico.thebrewingmarket.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public final class TBMExpansion extends PlaceholderExpansion {

    private final String identifier;
    private final JavaPlugin plugin;
    private final Supplier<MarketConfig> marketConfigSupplier;
    private final LangConfig langConfig;
    private final DailyEarningsTracker dailyTracker;
    private final PlayerStatsCache statsCache;
    private final MarketAnalyticsCache analyticsCache;
    private final SellService sellService;

    public TBMExpansion(String identifier,
                        JavaPlugin plugin,
                        Supplier<MarketConfig> marketConfigSupplier,
                        LangConfig langConfig,
                        DailyEarningsTracker dailyTracker,
                        PlayerStatsCache statsCache,
                        MarketAnalyticsCache analyticsCache,
                        SellService sellService) {
        this.identifier = identifier;
        this.plugin = plugin;
        this.marketConfigSupplier = marketConfigSupplier;
        this.langConfig = langConfig;
        this.dailyTracker = dailyTracker;
        this.statsCache = statsCache;
        this.analyticsCache = analyticsCache;
        this.sellService = sellService;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getAuthor() {
        return "CesarCosmico";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params == null) return "";
        String key = params.toLowerCase();

        MarketConfig config = marketConfigSupplier.get();
        LimitationConfig limit = config.getLimitation();

        switch (key) {
            case "limit_active":
                return String.valueOf(limit.active());
            case "daily_limit":
                return sellService.format(limit.earnings());
            case "daily_limit_raw":
                return raw(limit.earnings());
        }

        if (key.startsWith("price_")) {
            return resolvePrice(config, key.substring("price_".length()));
        }

        Analytics analytics = analyticsCache.getSnapshot();
        switch (key) {
            case "global_total_today":
                return sellService.format(analytics.totalToday());
            case "global_total_today_raw":
                return raw(analytics.totalToday());
            case "global_top_recipe_id_today":
                return notBlank(analytics.topRecipe(), langConfig.getPlaceholderEmpty(key));
            case "global_top_recipe_name_today": {
                String name = analytics.topRecipeName();
                if (name != null && !name.isEmpty()) return name;
                return notBlank(analytics.topRecipe(), langConfig.getPlaceholderEmpty(key));
            }
            case "global_top_recipe_today_qty":
                return String.valueOf(analytics.topRecipeQty());
            case "global_top_player_today":
                return notBlank(analytics.topPlayer(), langConfig.getPlaceholderEmpty(key));
            case "global_top_player_today_amount":
                return sellService.format(analytics.topPlayerAmount());
            case "global_top_player_today_amount_raw":
                return raw(analytics.topPlayerAmount());
        }

        if (player == null) return "";

        double earned = dailyTracker.getTodayEarnings(player.getUniqueId());
        double remaining = Math.max(0.0, limit.earnings() - earned);

        switch (key) {
            case "daily_earned":
                return sellService.format(earned);
            case "daily_earned_raw":
                return raw(earned);
            case "daily_remaining":
                return sellService.format(remaining);
            case "daily_remaining_raw":
                return raw(remaining);
            case "daily_percent": {
                if (limit.earnings() <= 0) return "0";
                int pct = (int) Math.round((earned / limit.earnings()) * 100.0);
                return String.valueOf(Math.max(0, Math.min(100, pct)));
            }
        }

        PlayerStats stats = statsCache.getStats(player.getUniqueId());
        switch (key) {
            case "lifetime_earned":
                return sellService.format(stats.lifetimeEarned());
            case "lifetime_earned_raw":
                return raw(stats.lifetimeEarned());
            case "lifetime_brews":
                return String.valueOf(stats.lifetimeBrews());
            case "last_sale_amount":
                return sellService.format(stats.lastAmount());
            case "last_sale_amount_raw":
                return raw(stats.lastAmount());
            case "last_sale_recipe_id":
                return notBlank(stats.lastRecipe(), langConfig.getPlaceholderEmpty(key));
            case "last_sale_recipe_name": {
                String name = stats.lastDisplayName();
                if (name != null && !name.isEmpty()) return name;
                return notBlank(stats.lastRecipe(), langConfig.getPlaceholderEmpty(key));
            }
            case "last_sale_ago":
                return stats.lastSoldAt() > 0
                        ? TimeUtil.relativeTime(stats.lastSoldAt())
                        : langConfig.getPlaceholderEmpty(key);
        }

        if (key.startsWith("player_sold_")) {
            return resolvePlayerSold(player.getUniqueId(), key.substring("player_sold_".length()));
        }

        return null;
    }

    private String resolvePrice(MarketConfig config, String rest) {
        boolean rawValue = rest.endsWith("_raw");
        String recipe = rawValue ? rest.substring(0, rest.length() - "_raw".length()) : rest;
        if (recipe.isEmpty()) return "";
        double price = config.getBasePrice(recipe);
        return rawValue ? raw(price) : sellService.format(price);
    }

    private String resolvePlayerSold(java.util.UUID uuid, String rest) {
        String recipe;
        String suffix;
        if (rest.endsWith("_total_raw")) {
            recipe = rest.substring(0, rest.length() - "_total_raw".length());
            suffix = "total_raw";
        } else if (rest.endsWith("_total")) {
            recipe = rest.substring(0, rest.length() - "_total".length());
            suffix = "total";
        } else if (rest.endsWith("_qty")) {
            recipe = rest.substring(0, rest.length() - "_qty".length());
            suffix = "qty";
        } else {
            recipe = rest;
            suffix = "";
        }
        if (recipe.isEmpty()) return "";

        RecipeTally tally = statsCache.recipeTally(uuid, recipe);
        return switch (suffix) {
            case "total" -> sellService.format(tally.total());
            case "total_raw" -> raw(tally.total());
            case "qty" -> String.valueOf(tally.quantity());
            default -> String.valueOf(tally.quantity() > 0L);
        };
    }

    private static String raw(double value) {
        return Double.toString(value);
    }

    private static String notBlank(String value, String fallback) {
        return value != null && !value.isEmpty() ? value : fallback;
    }
}
