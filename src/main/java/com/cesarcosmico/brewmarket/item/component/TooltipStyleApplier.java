package com.cesarcosmico.brewmarket.item.component;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Logger;

public class TooltipStyleApplier implements ComponentApplier {

    private final Logger logger;

    public TooltipStyleApplier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String key() {
        return "tooltip_style";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        String value = section.getString(key());
        if (value == null || value.isEmpty()) return;

        try {
            NamespacedKey styleKey = NamespacedKey.fromString(value);
            if (styleKey == null) {
                logger.warning("Invalid tooltip_style key: " + value);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            meta.setTooltipStyle(styleKey);
            item.setItemMeta(meta);
        } catch (Exception e) {
            logger.warning("Failed to apply tooltip_style '" + value + "': " + e.getMessage());
        }
    }
}