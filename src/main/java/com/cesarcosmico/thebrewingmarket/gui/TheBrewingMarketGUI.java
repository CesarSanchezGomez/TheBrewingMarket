package com.cesarcosmico.thebrewingmarket.gui;

import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class TheBrewingMarketGUI implements InventoryHolder {

    private final Inventory inventory;
    private final MarketConfig config;
    private final SellService sellService;
    private final Player owner;
    private final boolean shulkerEnabled;
    private BukkitTask refreshTask;

    public TheBrewingMarketGUI(MarketConfig config, SellService sellService, Player player,
                         boolean shulkerEnabled) {
        this.config = config;
        this.sellService = sellService;
        this.owner = player;
        this.shulkerEnabled = shulkerEnabled;

        this.inventory = Bukkit.createInventory(this, config.getInventorySize(), config.getTitle());

        populateDecoration();
        refreshSellButtons();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void startAutoRefresh(JavaPlugin plugin) {
        this.refreshTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, this::refreshSellButtons, 20L, 20L);
    }

    public void stopAutoRefresh() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public MarketConfig getConfig() {
        return config;
    }

    public SellService getSellService() {
        return sellService;
    }

    public boolean isShulkerEnabled() {
        return shulkerEnabled;
    }

    public void refreshSellButtons() {
        SellService.InventoryStats guiStats    = sellService.computeGuiStats(inventory, config, shulkerEnabled);
        SellService.InventoryStats playerStats = sellService.computePlayerStats(owner, shulkerEnabled);

        String guiMoney   = sellService.format(guiStats.value());
        String totalMoney = sellService.format(guiStats.value() + playerStats.value());

        int guiBrewCount   = guiStats.brewCount();
        int totalBrewCount = guiBrewCount + playerStats.brewCount();

        for (int slot : config.getSellSlots()) {
            if (guiBrewCount > 0) {
                inventory.setItem(slot, config.buildSellButton(
                        config.getSellAllow(), guiMoney, String.valueOf(guiBrewCount)));
            } else {
                inventory.setItem(slot, config.buildSellButton(
                        config.getSellDeny(), "0", "0"));
            }
        }

        for (int slot : config.getSellAllSlots()) {
            if (totalBrewCount > 0) {
                inventory.setItem(slot, config.buildSellButton(
                        config.getSellAllAllow(), totalMoney, String.valueOf(totalBrewCount)));
            } else {
                inventory.setItem(slot, config.buildSellButton(
                        config.getSellAllDeny(), "0", "0"));
            }
        }
    }

    public void returnItems(Player player) {
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            player.getInventory().addItem(item).forEach((idx, overflow) ->
                    player.getWorld().dropItemNaturally(player.getLocation(), overflow));
            inventory.setItem(slot, null);
        }
    }

    private void populateDecoration() {
        for (int slot = 0; slot < config.getInventorySize(); slot++) {
            if (config.isItemSlot(slot) || config.isSellSlot(slot) || config.isSellAllSlot(slot)) {
                continue;
            }
            char symbol = config.getSymbolAt(slot);
            ItemStack decoItem = config.getDecorativeItem(symbol);
            if (decoItem != null) {
                inventory.setItem(slot, decoItem.clone());
            }
        }
    }
}