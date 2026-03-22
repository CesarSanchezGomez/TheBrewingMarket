package com.cesarcosmico.brewmarket.item.component;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.logging.Logger;

public class PotionContentsApplier implements ComponentApplier {

    private final Logger logger;

    public PotionContentsApplier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String key() {
        return "potion_contents";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection potionSection = section.getConfigurationSection(key());
        if (potionSection == null) return;

        if (!(item.getItemMeta() instanceof PotionMeta potionMeta)) return;

        applyBasePotionType(potionSection, potionMeta);
        applyCustomColor(potionSection, potionMeta);
        applyCustomEffects(potionSection, potionMeta);

        item.setItemMeta(potionMeta);
    }

    private void applyBasePotionType(ConfigurationSection section, PotionMeta meta) {
        String potionId = section.getString("potion");
        if (potionId == null) return;

        NamespacedKey nsKey = NamespacedKey.minecraft(potionId.toLowerCase());
        PotionType type = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.POTION)
                .get(nsKey);

        if (type == null) {
            logger.warning("Unknown potion type: " + potionId);
            return;
        }

        meta.setBasePotionType(type);
    }

    private void applyCustomColor(ConfigurationSection section, PotionMeta meta) {
        if (section.contains("custom_color")) {
            meta.setColor(Color.fromRGB(section.getInt("custom_color")));
        }
    }

    private void applyCustomEffects(ConfigurationSection section, PotionMeta meta) {
        var effectsList = section.getMapList("custom_effects");
        for (var effectMap : effectsList) {
            String id = (String) effectMap.get("id");
            if (id == null) continue;

            NamespacedKey nsKey = NamespacedKey.minecraft(id.toLowerCase());
            PotionEffectType effectType = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.MOB_EFFECT)
                    .get(nsKey);

            if (effectType == null) {
                logger.warning("Unknown potion effect: " + id);
                continue;
            }

            int amplifier = effectMap.containsKey("amplifier") ? ((Number) effectMap.get("amplifier")).intValue() : 0;
            int duration = effectMap.containsKey("duration") ? ((Number) effectMap.get("duration")).intValue() : 1;
            boolean ambient = effectMap.containsKey("ambient") && (Boolean) effectMap.get("ambient");
            boolean particles = !effectMap.containsKey("show_particles") || (Boolean) effectMap.get("show_particles");
            boolean icon = !effectMap.containsKey("show_icon") || (Boolean) effectMap.get("show_icon");

            meta.addCustomEffect(new PotionEffect(effectType, duration, amplifier, ambient, particles, icon), true);
        }
    }
}