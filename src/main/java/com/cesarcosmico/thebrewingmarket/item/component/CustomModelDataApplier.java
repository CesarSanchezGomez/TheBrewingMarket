package com.cesarcosmico.thebrewingmarket.item.component;

import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class CustomModelDataApplier extends BaseComponentApplier {

    @Override
    public String key() {
        return "custom_model_data";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection cmdSection = section.getConfigurationSection(key());
        if (cmdSection == null) return;

        editMeta(item, meta -> {
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
        });
    }
}