package com.cesarcosmico.thebrewingmarket.config;

import com.cesarcosmico.thebrewingmarket.item.IconFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class MarketConfig {

    public static final int CURRENT_VERSION = 1;

    public record LimitationConfig(boolean enabled, double earnings) {
        public boolean active() { return enabled && earnings > 0; }
    }

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final LayoutParser layout;
    private final IconFactory iconFactory;
    private final Map<Character, ItemStack> decorativeItems;

    private final Component title;

    private final char itemSlotSymbol;
    private final char sellSymbol;
    private final char sellAllSymbol;
    private final char closeSymbol;

    private final IconConfig sellAllow;
    private final IconConfig sellDeny;
    private final IconConfig sellAllAllow;
    private final IconConfig sellAllDeny;

    private final boolean shulkerSelling;
    private final int historyPerPage;

    private final Map<String, Double> prices;
    private final double defaultPrice;

    private final LimitationConfig limitation;

    public MarketConfig(ConfigurationSection root, Logger logger) {
        this.historyPerPage = Math.clamp(root.getInt("history-per-page", 8), 1, 32);

        ConfigurationSection market = root.getConfigurationSection("market");

        this.title = MINI.deserialize(market.getString("title", "<gold><b>Brew Market</b></gold>"));
        this.shulkerSelling = market.getBoolean("shulker-selling", false);

        this.iconFactory = new IconFactory(logger);

        this.layout = new LayoutParser(market.getStringList("layout"), logger);

        this.itemSlotSymbol = market.getString("item-slot.symbol", "I").charAt(0);
        this.sellSymbol = market.getString("sell-icons.symbol", "B").charAt(0);
        this.sellAllSymbol = market.getString("sell-all-icons.symbol", "S").charAt(0);
        this.closeSymbol = market.getString("close-icon.symbol", "C").charAt(0);

        this.decorativeItems = iconFactory.parseDecorativeIcons(market);

        this.sellAllow = iconFactory.parseIconConfig(market.getConfigurationSection("sell-icons.allow-icon"));
        this.sellDeny = iconFactory.parseIconConfig(market.getConfigurationSection("sell-icons.deny-icon"));
        this.sellAllAllow = iconFactory.parseIconConfig(market.getConfigurationSection("sell-all-icons.allow-icon"));
        this.sellAllDeny = iconFactory.parseIconConfig(market.getConfigurationSection("sell-all-icons.deny-icon"));

        this.prices = new HashMap<>();
        ConfigurationSection priceSection = market.getConfigurationSection("prices");
        this.defaultPrice = priceSection != null ? priceSection.getDouble("default", 0.0) : 0.0;
        if (priceSection != null) {
            loadPrices(priceSection, logger);
        }

        ConfigurationSection lim = market.getConfigurationSection("limitation");
        this.limitation = new LimitationConfig(
                lim != null && lim.getBoolean("enable", false),
                lim != null ? lim.getDouble("earnings", 0.0) : 0.0);
    }

    private void loadPrices(ConfigurationSection priceSection, Logger logger) {
        ConfigurationSection groups = priceSection.getConfigurationSection("groups");
        if (groups != null) {
            // getValues(false) avoids treating dotted keys (e.g. "12.5") as config paths.
            for (Map.Entry<String, Object> entry : groups.getValues(false).entrySet()) {
                String priceKey = entry.getKey();
                double price;
                try {
                    price = Double.parseDouble(priceKey);
                } catch (NumberFormatException ex) {
                    logger.warning("Invalid price group key '" + priceKey + "' in prices.groups — expected a number.");
                    continue;
                }
                List<String> recipes = coerceRecipeList(entry.getValue());
                if (recipes.isEmpty()) {
                    logger.warning("Price group '" + priceKey + "' has no recipes assigned.");
                    continue;
                }
                for (String recipe : recipes) {
                    prices.put(recipe.toLowerCase(), price);
                }
            }
        }

        for (String key : priceSection.getKeys(false)) {
            if (key.equals("default") || key.equals("groups")) {
                continue;
            }
            if (priceSection.isConfigurationSection(key) || priceSection.isList(key)) {
                logger.warning("Unexpected value for price '" + key + "' — expected a number. "
                        + "Did you mean to put it inside 'groups'?");
                continue;
            }
            prices.put(key.toLowerCase(), priceSection.getDouble(key));
        }
    }

    private static List<String> coerceRecipeList(Object value) {
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

    public Component getTitle() {
        return title;
    }

    public int getInventorySize() {
        return layout.getInventorySize();
    }

    public Set<Integer> getItemSlots() {
        return layout.getSlotsForSymbol(itemSlotSymbol);
    }

    public boolean isItemSlot(int slot) {
        return getItemSlots().contains(slot);
    }

    public Set<Integer> getSellSlots() {
        return layout.getSlotsForSymbol(sellSymbol);
    }

    public boolean isSellSlot(int slot) {
        return getSellSlots().contains(slot);
    }

    public Set<Integer> getSellAllSlots() {
        return layout.getSlotsForSymbol(sellAllSymbol);
    }

    public boolean isSellAllSlot(int slot) {
        return getSellAllSlots().contains(slot);
    }

    public boolean isCloseSlot(int slot) {
        return layout.getSlotsForSymbol(closeSymbol).contains(slot);
    }

    public char getSymbolAt(int slot) {
        return layout.getSymbolAt(slot);
    }

    public ItemStack getDecorativeItem(char symbol) {
        return decorativeItems.get(symbol);
    }

    public ItemStack buildSellButton(IconConfig config, String money, String soldAmount) {
        return iconFactory.buildDynamicIcon(config, money, soldAmount);
    }

    public String getActionSound(IconConfig config) {
        return config.sound();
    }

    public boolean isShulkerSellingEnabled() {
        return shulkerSelling;
    }

    public int getHistoryPerPage() {
        return historyPerPage;
    }

    public double getBasePrice(String recipeName) {
        return prices.getOrDefault(recipeName.toLowerCase(), defaultPrice);
    }

    public IconConfig getSellAllow() {
        return sellAllow;
    }

    public IconConfig getSellDeny() {
        return sellDeny;
    }

    public IconConfig getSellAllAllow() {
        return sellAllAllow;
    }

    public IconConfig getSellAllDeny() {
        return sellAllDeny;
    }

    public LimitationConfig getLimitation() {
        return limitation;
    }
}