package com.cesarcosmico.thebrewingmarket.item.component;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public final class PotionContentsApplier implements ComponentApplier {

    @Override
    public String key() {
        return "potion-contents";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection potionSection = section.getConfigurationSection(key());
        if (potionSection == null) return;

        if (!(item.getItemMeta() instanceof PotionMeta potionMeta)) return;

        if (potionSection.contains("custom-color")) {
            potionMeta.setColor(Color.fromRGB(potionSection.getInt("custom-color")));
        }

        item.setItemMeta(potionMeta);
    }
}