package com.cesarcosmico.brewmarket.service;

import com.cesarcosmico.brewmarket.config.MarketConfig;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.function.Supplier;

public class BrewPriceService {

    private static final NamespacedKey SCORE_KEY = new NamespacedKey("brewery", "score");

    private final MarketConfig marketConfig;
    private final Supplier<TheBrewingProjectApi> apiSupplier;

    public BrewPriceService(MarketConfig marketConfig, Supplier<TheBrewingProjectApi> apiSupplier) {
        this.marketConfig = marketConfig;
        this.apiSupplier = apiSupplier;
    }

    public record BrewEvaluation(String recipeName, double score, double price) {
    }

    public Optional<BrewEvaluation> evaluate(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return Optional.empty();
        }

        TheBrewingProjectApi api = apiSupplier.get();
        if (api == null) {
            return Optional.empty();
        }

        BrewManager<ItemStack> brewManager = api.getBrewManager();
        Optional<Brew> brewOpt = brewManager.fromItem(itemStack);
        if (brewOpt.isEmpty()) {
            return Optional.empty();
        }

        Brew brew = brewOpt.get();
        Optional<Recipe<ItemStack>> recipeOpt = brew.closestRecipe(api.getRecipeRegistry());
        if (recipeOpt.isEmpty()) {
            return Optional.empty();
        }

        String recipeName = recipeOpt.get().getRecipeName();
        double score = extractScore(itemStack);
        if (score <= 0) {
            return Optional.empty();
        }

        double basePrice = marketConfig.getBasePrice(recipeName);
        if (basePrice <= 0) {
            return Optional.empty();
        }

        return Optional.of(new BrewEvaluation(recipeName, score, basePrice * score));
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
}