package com.cesarcosmico.thebrewingmarket.brew;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class BreweryXBrewResolver implements BrewResolver {

    private static final double MAX_QUALITY = 10.0;

    @Override
    public Optional<String> resolveRecipeName(final ItemStack item) {
        final Brew brew = Brew.get(item);
        if (brew != null) {
            final BRecipe recipe = brew.getCurrentRecipe();
            if (recipe != null) {
                final String id = recipe.getId();
                final String name = id != null ? id : recipe.getRecipeName();
                if (name != null && !name.isEmpty()) {
                    return Optional.of(name);
                }
            }
        }
        return BreweryXRawDecoder.decode(item)
                .map(BreweryXRawDecoder.RawBrewData::recipeName)
                .filter(name -> name != null && !name.isEmpty());
    }

    @Override
    public double resolveScore(final ItemStack item) {
        final Brew brew = Brew.get(item);
        if (brew != null) {
            return brew.getQuality() / MAX_QUALITY;
        }
        return BreweryXRawDecoder.decode(item)
                .map(data -> data.quality() / MAX_QUALITY)
                .orElse(0.0);
    }
}