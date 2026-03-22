package com.cesarcosmico.brewmarket.item.component;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Logger;

public class ItemModelApplier implements ComponentApplier {

    private final Logger logger;

    public ItemModelApplier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String key() {
        return "item_model";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        String value = section.getString(key());
        if (value == null || value.isEmpty()) return;

        try {
            NamespacedKey modelKey = NamespacedKey.fromString(value);
            if (modelKey == null) {
                logger.warning("Invalid item_model key: " + value);
                return;
            }

            ItemMeta meta = item.getItemMeta();
            meta.setItemModel(modelKey);
            item.setItemMeta(meta);
        } catch (Exception e) {
            logger.warning("Failed to apply item_model '" + value + "': " + e.getMessage());
        }
    }
}