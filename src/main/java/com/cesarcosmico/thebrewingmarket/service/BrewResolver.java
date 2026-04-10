package com.cesarcosmico.thebrewingmarket.service;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface BrewResolver {

    Optional<String> resolveRecipeName(ItemStack item);

    double resolveScore(ItemStack item);
}