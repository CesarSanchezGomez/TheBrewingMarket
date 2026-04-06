package com.cesarcosmico.thebrewingmarket.item.component;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public abstract class BaseComponentApplier implements ComponentApplier {

    protected final void editMeta(ItemStack item, Consumer<ItemMeta> modifier) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        modifier.accept(meta);
        item.setItemMeta(meta);
    }
}
