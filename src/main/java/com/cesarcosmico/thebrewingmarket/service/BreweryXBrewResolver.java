package com.cesarcosmico.thebrewingmarket.service;

import com.dre.brewery.Brew;
import com.dre.brewery.recipe.BRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class BreweryXBrewResolver implements BrewResolver {

    private static final double MAX_QUALITY = 10.0;

    @Override
    public Optional<String> resolveRecipeName(final ItemStack item) {
        final Brew brew = Brew.get(item);
        if (brew == null) {
            return Optional.empty();
        }
        final BRecipe recipe = brew.getCurrentRecipe();
        if (recipe == null) {
            return Optional.empty();
        }
        final String id = recipe.getId();
        return id != null ? Optional.of(id) : Optional.ofNullable(recipe.getRecipeName());
    }

    @Override
    public double resolveScore(final ItemStack item) {
        final Brew brew = Brew.get(item);
        if (brew == null) {
            return 0.0;
        }
        return brew.getQuality() / MAX_QUALITY;
    }
}