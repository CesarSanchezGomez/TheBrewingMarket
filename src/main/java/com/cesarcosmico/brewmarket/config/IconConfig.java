package com.cesarcosmico.brewmarket.config;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record IconConfig(
        ItemStack baseItem,
        String sound,
        String message,
        List<String> loreRaw,
        String displayNameRaw
) {
}