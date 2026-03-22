package com.cesarcosmico.brewmarket.item.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LoreApplier implements ComponentApplier {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    @Override
    public String key() {
        return "lore";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        List<String> lines = section.getStringList(key());
        if (lines.isEmpty()) return;

        List<Component> lore = lines.stream()
                .map(line -> MINI.deserialize(line).decoration(TextDecoration.ITALIC, false))
                .toList();

        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }
}