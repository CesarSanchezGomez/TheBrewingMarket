package com.cesarcosmico.thebrewingmarket.brew;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class CompositeBrewResolver implements BrewResolver {

    private final List<BrewResolver> resolvers;

    public CompositeBrewResolver(final List<BrewResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    @Override
    public Optional<ResolvedItem> resolve(final ItemStack item) {
        for (final BrewResolver resolver : resolvers) {
            final Optional<ResolvedItem> resolved = resolver.resolve(item);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
    }
}