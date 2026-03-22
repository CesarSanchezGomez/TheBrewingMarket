package com.cesarcosmico.brewmarket.item.component;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomNameApplier implements ComponentApplier {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    @Override
    public String key() {
        return "custom_name";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        String raw = section.getString(key());
        if (raw == null) return;

        ItemMeta meta = item.getItemMeta();
        meta.displayName(MINI.deserialize(raw).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
    }
}