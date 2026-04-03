package com.cesarcosmico.brewmarket.service;

import com.cesarcosmico.brewmarket.config.MarketConfig;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.function.Supplier;

public class BrewPriceService {

    private static final NamespacedKey SCORE_KEY = new NamespacedKey("brewery", "score");
    private static final NamespacedKey TAG_KEY = new NamespacedKey("brewery", "tag");

    private final MarketConfig marketConfig;
    private final Supplier<TheBrewingProjectApi> apiSupplier;

    public BrewPriceService(MarketConfig marketConfig, Supplier<TheBrewingProjectApi> apiSupplier) {
        this.marketConfig = marketConfig;
        this.apiSupplier = apiSupplier;
    }

    public record BrewEvaluation(String recipeName, double score, double price, Component displayName) {
    }

    public Optional<BrewEvaluation> evaluate(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }

        String recipeName = resolveRecipeName(itemStack);
        if (recipeName == null) {
            return Optional.empty();
        }

        double score = extractScore(itemStack);
        if (score <= 0) {
            return Optional.empty();
        }

        double basePrice = marketConfig.getBasePrice(recipeName);
        if (basePrice <= 0) {
            return Optional.empty();
        }

        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = (meta != null && meta.hasDisplayName())
                ? meta.displayName()
                : Component.text(recipeName);

        return Optional.of(new BrewEvaluation(recipeName, score, basePrice * score, displayName));
    }

    private String resolveRecipeName(ItemStack itemStack) {
        TheBrewingProjectApi api = apiSupplier.get();
        if (api != null) {
            Optional<Brew> brewOpt = api.getBrewManager().fromItem(itemStack);
            if (brewOpt.isPresent()) {
                Optional<Recipe<ItemStack>> recipeOpt = brewOpt.get().closestRecipe(api.getRecipeRegistry());
                if (recipeOpt.isPresent()) {
                    return recipeOpt.get().getRecipeName();
                }
            }
        }

        return extractTag(itemStack);
    }

    private double extractScore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return 0.0;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(SCORE_KEY, PersistentDataType.DOUBLE)) {
            Double score = pdc.get(SCORE_KEY, PersistentDataType.DOUBLE);
            return score != null ? score : 0.0;
        }
        return 0.0;
    }

    private String extractTag(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(TAG_KEY, PersistentDataType.STRING)) {
            return pdc.get(TAG_KEY, PersistentDataType.STRING);
        }
        return null;
    }
}