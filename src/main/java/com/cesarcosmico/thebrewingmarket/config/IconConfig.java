package com.cesarcosmico.thebrewingmarket.config;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record IconConfig(
        ItemStack baseItem,
        String sound,
        List<String> loreRaw,
        String displayNameRaw
) {
    public boolean dynamic() {
        if (displayNameRaw != null && displayNameRaw.contains("%")) {
            return true;
        }
        if (loreRaw != null) {
            for (String line : loreRaw) {
                if (line != null && line.contains("%")) {
                    return true;
                }
            }
        }
        return false;
    }
}