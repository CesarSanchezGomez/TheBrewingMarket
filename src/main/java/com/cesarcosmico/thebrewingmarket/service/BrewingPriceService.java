package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public final class BrewingPriceService {

    private final MarketConfig marketConfig;
    private final BrewResolver brewResolver;

    public BrewingPriceService(final MarketConfig marketConfig, final BrewResolver brewResolver) {
        this.marketConfig = marketConfig;
        this.brewResolver = brewResolver;
    }

    public record BrewEvaluation(String recipeName, double score, double price, Component displayName) {
    }

    public Optional<BrewEvaluation> evaluate(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }

        final Optional<String> recipeName = brewResolver.resolveRecipeName(itemStack);
        if (recipeName.isEmpty()) {
            return Optional.empty();
        }

        final double score = brewResolver.resolveScore(itemStack);
        if (score <= 0) {
            return Optional.empty();
        }

        final double basePrice = marketConfig.getBasePrice(recipeName.get());
        if (basePrice <= 0) {
            return Optional.empty();
        }

        final ItemMeta meta = itemStack.getItemMeta();
        final Component displayName = (meta != null && meta.hasDisplayName())
                ? meta.displayName()
                : Component.text(recipeName.get());

        return Optional.of(new BrewEvaluation(recipeName.get(), score, basePrice * score, displayName));
    }
}