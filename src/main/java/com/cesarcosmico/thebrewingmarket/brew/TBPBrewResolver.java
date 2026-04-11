package com.cesarcosmico.thebrewingmarket.brew;

import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.function.Supplier;

public final class TBPBrewResolver implements BrewResolver {

    private static final NamespacedKey TAG_KEY   = new NamespacedKey("brewery", "tag");
    private static final NamespacedKey SCORE_KEY = new NamespacedKey("brewery", "score");

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
        return readTag(item, TAG_KEY, PersistentDataType.STRING);
    }

    @Override
    public double resolveScore(final ItemStack item) {
        final TheBrewingProjectApi api = apiSupplier.get();
        if (api != null) {
            final Optional<Double> score = api.getBrewManager().fromItem(item)
                    .flatMap(brew -> brew.closestRecipe(api.getRecipeRegistry())
                            .map(recipe -> brew.score(recipe).score()));
            if (score.isPresent()) {
                return score.get();
            }
        }
        return readTag(item, SCORE_KEY, PersistentDataType.DOUBLE).orElse(0.0);
    }

    private <T> Optional<T> readTag(final ItemStack item, final NamespacedKey key,
                                    final PersistentDataType<?, T> type) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.getPersistentDataContainer().get(key, type));
    }
}
