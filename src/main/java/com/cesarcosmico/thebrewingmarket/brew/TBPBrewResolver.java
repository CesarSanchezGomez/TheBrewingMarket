package com.cesarcosmico.thebrewingmarket.brew;

import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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
    public Optional<ResolvedItem> resolve(final ItemStack item) {
        final TheBrewingProjectApi api = apiSupplier.get();
        if (api != null) {
            final Optional<ResolvedItem> resolved = resolveFromApi(api, item);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return resolveFromTags(item);
    }

    private Optional<ResolvedItem> resolveFromApi(final TheBrewingProjectApi api, final ItemStack item) {
        try {
            return api.getBrewManager().fromItem(item)
                    .flatMap(brew -> brew.closestRecipe(api.getRecipeRegistry())
                            .map(recipe -> new ResolvedItem(recipe.getRecipeName(), brew.score(recipe).score(), PriceCategory.BREW)));        } catch (final RuntimeException ex) {
            return Optional.empty();
        }
    }

    private Optional<ResolvedItem> resolveFromTags(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        final String recipeName = pdc.get(TAG_KEY, PersistentDataType.STRING);
        if (recipeName == null) {
            return Optional.empty();
        }
        final Double score = pdc.get(SCORE_KEY, PersistentDataType.DOUBLE);
        return Optional.of(new ResolvedItem(recipeName, score != null ? score : 0.0, PriceCategory.BREW));    }
}