package com.cesarcosmico.brewmarket.gui;

import com.cesarcosmico.brewmarket.config.MarketConfig;
import com.cesarcosmico.brewmarket.service.SellService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BrewMarketGUI implements InventoryHolder {

    private final Inventory inventory;
    private final MarketConfig config;
    private final SellService sellService;
    private final Player owner;

    public BrewMarketGUI(MarketConfig config, SellService sellService, Player player) {
        this.config = config;
        this.sellService = sellService;
        this.owner = player;

        this.inventory = Bukkit.createInventory(this, config.getInventorySize(), config.getTitle());

        populateDecoration();
        refreshSellButtons();
    }

    public void open(Player player) {
        player.openInventory(inventory);
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

    public void refreshSellButtons() {
        double guiValue = sellService.calculateValue(inventory, config);
        double totalValue = sellService.calculateTotalValue(inventory, config, owner);

        String guiMoney = sellService.format(guiValue);
        String totalMoney = sellService.format(totalValue);

        int guiBrewCount = sellService.countBrews(inventory, config);
        int totalBrewCount = guiBrewCount + sellService.countBrewsInPlayerInventory(owner);

        for (int slot = 0; slot < config.getInventorySize(); slot++) {
            if (config.isSellSlot(slot)) {
                if (guiBrewCount > 0) {
                    inventory.setItem(slot, config.buildSellButton(
                            config.getSellAllow(), guiMoney, String.valueOf(guiBrewCount)));
                } else {
                    inventory.setItem(slot, config.buildSellButton(
                            config.getSellDeny(), "0", "0"));
                }
            }

            if (config.isSellAllSlot(slot)) {
                if (totalBrewCount > 0) {
                    inventory.setItem(slot, config.buildSellButton(
                            config.getSellAllAllow(), totalMoney, String.valueOf(totalBrewCount)));
                } else {
                    inventory.setItem(slot, config.buildSellButton(
                            config.getSellAllDeny(), "0", "0"));
                }
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