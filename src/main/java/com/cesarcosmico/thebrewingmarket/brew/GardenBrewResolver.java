package com.cesarcosmico.thebrewingmarket.brew;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.Optional;

public final class GardenBrewResolver implements BrewResolver {

    private static final double FIXED_SCORE = 1.0;
    private static final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey("garden", "item_type");
    private static final NamespacedKey PLANT_TYPE_KEY = new NamespacedKey("garden", "plant_type");

    @Override
    public Optional<ResolvedItem> resolve(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }
        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        final String itemType = pdc.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        final String plantType = pdc.get(PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (itemType == null || plantType == null) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedItem(toId(plantType, itemType), FIXED_SCORE, PriceCategory.GARDEN));
    }

    private static String toId(final String plantType, final String itemType) {
        final int separator = plantType.indexOf(':');
        final String plant = separator >= 0 ? plantType.substring(separator + 1) : plantType;
        return (plant + "_" + itemType).toLowerCase(Locale.ROOT);
    }
}