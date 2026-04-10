package com.cesarcosmico.thebrewingmarket.service;

import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.function.Supplier;

public final class TBPBrewResolver implements BrewResolver {

    private static final NamespacedKey SCORE_KEY = new NamespacedKey("brewery", "score");
    private static final NamespacedKey TAG_KEY   = new NamespacedKey("brewery", "tag");

    private final Supplier<TheBrewingProjectApi> apiSupplier;

    public TBPBrewResolver(final Supplier<TheBrewingProjectApi> apiSupplier) {
        this.apiSupplier = apiSupplier;
    }

    @Override
    public Optional<String> resolveRecipeName(final ItemStack item) {
        final TheBrewingProjectApi api = apiSupplier.get();
        if (api != null) {
            final Optional<String> name = api.getBrewManager().fromItem(item)
                    .flatMap(brew -> brew.closestRecipe(api.getRecipeRegistry()))
                    .map(Recipe::getRecipeName);
            if (name.isPresent()) {
                return name;
            }
        }
        return extractTag(item);
    }

    @Override
    public double resolveScore(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0.0;
        }
        final Double score = meta.getPersistentDataContainer().get(SCORE_KEY, PersistentDataType.DOUBLE);
        return score != null ? score : 0.0;
    }

    private Optional<String> extractTag(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        final String tag = meta.getPersistentDataContainer().get(TAG_KEY, PersistentDataType.STRING);
        return Optional.ofNullable(tag);
    }
}