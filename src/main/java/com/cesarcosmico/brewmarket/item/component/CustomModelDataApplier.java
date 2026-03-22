package com.cesarcosmico.brewmarket.item.component;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public class CustomModelDataApplier implements ComponentApplier {

    @Override
    public String key() {
        return "custom_model_data";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection cmdSection = section.getConfigurationSection(key());
        if (cmdSection == null) return;

        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();

        List<Float> floats = cmdSection.getFloatList("floats");
        if (!floats.isEmpty()) {
            cmd.setFloats(floats);
        }

        List<Boolean> flags = cmdSection.getBooleanList("flags");
        if (!flags.isEmpty()) {
            cmd.setFlags(flags);
        }

        List<String> strings = cmdSection.getStringList("strings");
        if (!strings.isEmpty()) {
            cmd.setStrings(strings);
        }

        List<Integer> colors = cmdSection.getIntegerList("colors");
        if (!colors.isEmpty()) {
            cmd.setColors(colors.stream().map(Color::fromRGB).toList());
        }

        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
    }
}