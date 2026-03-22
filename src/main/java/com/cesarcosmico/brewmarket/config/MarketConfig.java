package com.cesarcosmico.brewmarket.config;

import com.cesarcosmico.brewmarket.item.IconFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class MarketConfig {

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

    private final Map<String, Double> prices;
    private final double defaultPrice;

    public MarketConfig(ConfigurationSection root, Logger logger) {
        ConfigurationSection market = root.getConfigurationSection("market");

        this.title = MINI.deserialize(market.getString("title", "<gold><b>Brew Market</b></gold>"));

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
        if (priceSection != null) {
            for (String key : priceSection.getKeys(false)) {
                if (!key.equals("default")) {
                    prices.put(key.toLowerCase(), priceSection.getDouble(key));
                }
            }
        }
        this.defaultPrice = priceSection != null ? priceSection.getDouble("default", 0.0) : 0.0;
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

    public boolean isSellSlot(int slot) {
        return layout.getSlotsForSymbol(sellSymbol).contains(slot);
    }

    public boolean isSellAllSlot(int slot) {
        return layout.getSlotsForSymbol(sellAllSymbol).contains(slot);
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
}