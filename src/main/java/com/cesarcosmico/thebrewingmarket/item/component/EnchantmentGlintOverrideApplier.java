package com.cesarcosmico.thebrewingmarket.item.component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentGlintOverrideApplier implements ComponentApplier {

    @Override
    public String key() {
        return "enchantment_glint_override";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        if (!section.contains(key())) return;

        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(section.getBoolean(key()));
        item.setItemMeta(meta);
    }
}