package com.cesarcosmico.thebrewingmarket.brew;

import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public final class TBPBrewResolver implements BrewResolver {

    private final Supplier<TheBrewingProjectApi> apiSupplier;

    public TBPBrewResolver(final Supplier<TheBrewingProjectApi> apiSupplier) {
        this.apiSupplier = apiSupplier;
    }

    @Override
    public Optional<String> resolveRecipeName(final ItemStack item) {
        final TheBrewingProjectApi api = apiSupplier.get();
        if (api == null) {
            return Optional.empty();
        }
        return api.getBrewManager().fromItem(item)
                .flatMap(brew -> brew.closestRecipe(api.getRecipeRegistry()))
                .map(Recipe::getRecipeName);
    }

    @Override
    public double resolveScore(final ItemStack item) {
        final TheBrewingProjectApi api = apiSupplier.get();
        if (api == null) {
            return 0.0;
        }
        return api.getBrewManager().fromItem(item)
                .flatMap(brew -> brew.closestRecipe(api.getRecipeRegistry())
                        .map(recipe -> brew.score(recipe).score()))
                .orElse(0.0);
    }
}
