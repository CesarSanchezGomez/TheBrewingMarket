package com.cesarcosmico.brewmarket.item.component;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Logger;

public class EnchantmentsApplier implements ComponentApplier {

    private final Logger logger;

    public EnchantmentsApplier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String key() {
        return "enchantments";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection enchSection = section.getConfigurationSection(key());
        if (enchSection == null) return;

        ItemMeta meta = item.getItemMeta();

        for (String enchantKey : enchSection.getKeys(false)) {
            int level = enchSection.getInt(enchantKey, 1);

            NamespacedKey nsKey = NamespacedKey.minecraft(enchantKey.toLowerCase());
            Enchantment enchantment = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .get(nsKey);

            if (enchantment == null) {
                logger.warning("Unknown enchantment: " + enchantKey);
                continue;
            }

            meta.addEnchant(enchantment, level, true);
        }

        item.setItemMeta(meta);
    }
}