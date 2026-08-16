package com.cesarcosmico.thebrewingmarket.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public final class PriceTable {

    private final Map<String, Double> prices;
    private final double defaultPrice;

    private PriceTable(Map<String, Double> prices, double defaultPrice) {
        this.prices = prices;
        this.defaultPrice = defaultPrice;
    }

    public static PriceTable load(ConfigurationSection section, String sectionName, Logger logger) {
        if (section == null) {
            return new PriceTable(Map.of(), 0.0);
        }
        Map<String, Double> prices = new HashMap<>();
        loadGroups(section, sectionName, prices, logger);
        loadFlatEntries(section, sectionName, prices, logger);
        return new PriceTable(prices, section.getDouble("default", 0.0));
    }

    public double get(String id) {
        return prices.getOrDefault(id.toLowerCase(Locale.ROOT), defaultPrice);
    }

    private static void loadGroups(ConfigurationSection section, String sectionName,
                                   Map<String, Double> prices, Logger logger) {
        ConfigurationSection groups = section.getConfigurationSection("groups");
        if (groups == null) {
            return;
        }
        // getValues(false) avoids treating dotted keys (e.g. "12.5") as config paths.
        for (Map.Entry<String, Object> entry : groups.getValues(false).entrySet()) {
            String priceKey = entry.getKey();
            double price;
            try {
                price = Double.parseDouble(priceKey);
            } catch (NumberFormatException ex) {
                logger.warning("Invalid price group key '" + priceKey + "' in " + sectionName
                        + ".groups — expected a number.");
                continue;
            }
            List<String> ids = coerceIdList(entry.getValue());
            if (ids.isEmpty()) {
                logger.warning("Price group '" + priceKey + "' in " + sectionName + " has no entries assigned.");
                continue;
            }
            for (String id : ids) {
                prices.put(id.toLowerCase(Locale.ROOT), price);
            }
        }
    }

    private static void loadFlatEntries(ConfigurationSection section, String sectionName,
                                        Map<String, Double> prices, Logger logger) {
        for (String key : section.getKeys(false)) {
            if (key.equals("default") || key.equals("groups")) {
                continue;
            }
            if (section.isConfigurationSection(key) || section.isList(key)) {
                logger.warning("Unexpected value for '" + key + "' in " + sectionName
                        + " — expected a number. Did you mean to put it inside 'groups'?");
                continue;
            }
            prices.put(key.toLowerCase(Locale.ROOT), section.getDouble(key));
        }
    }

    private static List<String> coerceIdList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>(list.size());
            for (Object item : list) {
                if (item != null) {
                    String name = item.toString().trim();
                    if (!name.isEmpty()) {
                        result.add(name);
                    }
                }
            }
            return result;
        }
        if (value instanceof String single) {
            String trimmed = single.trim();
            return trimmed.isEmpty() ? List.of() : List.of(trimmed);
        }
        return List.of();
    }
}