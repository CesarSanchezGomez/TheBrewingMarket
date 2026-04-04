package com.cesarcosmico.thebrewingmarket.util;

import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public final class ShulkerUtil {

    private ShulkerUtil() {
    }

    public static boolean isShulkerBox(ItemStack item) {
        if (item == null) return false;
        if (!(item.getItemMeta() instanceof BlockStateMeta bsm)) return false;
        return bsm.getBlockState() instanceof ShulkerBox;
    }
}