package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.brew.BrewResolver;
import com.cesarcosmico.thebrewingmarket.brew.ResolvedItem;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public final class BrewEvaluator {

    private final MarketConfig marketConfig;
    private final BrewResolver brewResolver;

    public BrewEvaluator(final MarketConfig marketConfig, final BrewResolver brewResolver) {
        this.marketConfig = marketConfig;
        this.brewResolver = brewResolver;
    }

    public record BrewEvaluation(String recipeId, double score, double price, Component displayName) {
    }

    public Optional<BrewEvaluation> evaluate(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }

        final Optional<ResolvedItem> resolved = brewResolver.resolve(itemStack);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }

        final ResolvedItem item = resolved.get();
        if (item.score() <= 0) {
            return Optional.empty();
        }

        final double basePrice = marketConfig.getBasePrice(item.id());
        if (basePrice <= 0) {
            return Optional.empty();
        }

        final ItemMeta meta = itemStack.getItemMeta();
        final Component displayName = (meta != null && meta.hasDisplayName())
                ? meta.displayName()
                : Component.text(item.id());

        return Optional.of(new BrewEvaluation(item.id(), item.score(), basePrice * item.score(), displayName));
    }
}