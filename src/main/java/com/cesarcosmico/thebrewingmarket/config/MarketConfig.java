package com.cesarcosmico.thebrewingmarket.config;

import com.cesarcosmico.thebrewingmarket.brew.PriceCategory;
import com.cesarcosmico.thebrewingmarket.item.IconFactory;
import com.cesarcosmico.thebrewingmarket.service.MoneyFormatter;
import com.cesarcosmico.thebrewingmarket.text.TextRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class MarketConfig {

    public static final int CURRENT_VERSION = 2;

    public record LimitationConfig(boolean enabled, double earnings) {
        public boolean active() { return enabled && earnings > 0; }
    }

    private final TextRenderer textRenderer;
    private final LayoutParser layout;
    private final IconFactory iconFactory;
    private final Map<Character, IconConfig> decorativeIcons;

    private final String titleRaw;
    private final String moneyFormatPattern;

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

    private final PriceTable brewPrices;
    private final PriceTable gardenPrices;

    private final LimitationConfig limitation;

    public MarketConfig(ConfigurationSection root, Logger logger, TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.historyPerPage = Math.clamp(root.getInt("history-per-page", 8), 1, 32);

        ConfigurationSection market = root.getConfigurationSection("market");

        this.titleRaw = market.getString("title", "<gold><b>Brew Market</b></gold>");
        this.shulkerSelling = market.getBoolean("shulker-selling", false);
        this.moneyFormatPattern = market.getString("money-format", MoneyFormatter.DEFAULT_PATTERN);

        this.iconFactory = new IconFactory(logger, textRenderer);

        this.layout = new LayoutParser(market.getStringList("layout"), logger);

        this.itemSlotSymbol = market.getString("item-slot.symbol", "I").charAt(0);
        this.sellSymbol = market.getString("sell-icons.symbol", "B").charAt(0);
        this.sellAllSymbol = market.getString("sell-all-icons.symbol", "S").charAt(0);
        this.closeSymbol = market.getString("close-icon.symbol", "C").charAt(0);

        this.decorativeIcons = iconFactory.parseDecorativeIcons(market);

        this.sellAllow = iconFactory.parseIconConfig(market.getConfigurationSection("sell-icons.allow-icon"));
        this.sellDeny = iconFactory.parseIconConfig(market.getConfigurationSection("sell-icons.deny-icon"));
        this.sellAllAllow = iconFactory.parseIconConfig(market.getConfigurationSection("sell-all-icons.allow-icon"));
        this.sellAllDeny = iconFactory.parseIconConfig(market.getConfigurationSection("sell-all-icons.deny-icon"));

        this.brewPrices = PriceTable.load(market.getConfigurationSection("brews"), "brews", logger);
        this.gardenPrices = PriceTable.load(market.getConfigurationSection("garden"), "garden", logger);

        ConfigurationSection lim = market.getConfigurationSection("limitation");
        this.limitation = new LimitationConfig(
                lim != null && lim.getBoolean("enable", false),
                lim != null ? lim.getDouble("earnings", 0.0) : 0.0);
    }

    public Component renderTitle(OfflinePlayer viewer) {
        return textRenderer.render(viewer, titleRaw);
    }

    public String getMoneyFormatPattern() {
        return moneyFormatPattern;
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

    public ItemStack renderDecoration(char symbol, OfflinePlayer viewer) {
        IconConfig icon = decorativeIcons.get(symbol);
        return icon == null ? null : iconFactory.render(icon, viewer, TagResolver.empty());
    }

    public boolean isDynamicDecoration(char symbol) {
        IconConfig icon = decorativeIcons.get(symbol);
        return icon != null && icon.dynamic();
    }

    public ItemStack buildSellButton(IconConfig config, OfflinePlayer viewer, String money, String soldAmount) {
        return iconFactory.render(config, viewer, TagResolver.resolver(
                Placeholder.unparsed("money", money),
                Placeholder.unparsed("sold-amount", soldAmount)));
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

    public double getBasePrice(PriceCategory category, String id) {
        return switch (category) {
            case BREW -> brewPrices.get(id);
            case GARDEN -> gardenPrices.get(id);
        };
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