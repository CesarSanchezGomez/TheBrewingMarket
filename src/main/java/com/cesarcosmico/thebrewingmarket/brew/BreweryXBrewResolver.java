package com.cesarcosmico.thebrewingmarket.brew;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class BreweryXBrewResolver implements BrewResolver {

    private static final double MAX_QUALITY = 10.0;

    @Override
    public Optional<ResolvedItem> resolve(final ItemStack item) {
        final Brew brew = Brew.get(item);
        if (brew != null) {
            final BRecipe recipe = brew.getCurrentRecipe();
            if (recipe != null) {
                final String id = recipe.getId();
                final String name = id != null ? id : recipe.getRecipeName();
                if (name != null && !name.isEmpty()) {
                    return Optional.of(new ResolvedItem(name, brew.getQuality() / MAX_QUALITY, PriceCategory.BREW));
                }
            }
        }
        return BreweryXRawDecoder.decode(item)
                .filter(data -> data.recipeName() != null && !data.recipeName().isEmpty())
                .map(data -> new ResolvedItem(data.recipeName(), data.quality() / MAX_QUALITY, PriceCategory.BREW));
    }
}