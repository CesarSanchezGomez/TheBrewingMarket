package com.cesarcosmico.thebrewingmarket.config;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record IconConfig(
        ItemStack baseItem,
        String sound,
        List<String> loreRaw,
        String displayNameRaw
) {
}