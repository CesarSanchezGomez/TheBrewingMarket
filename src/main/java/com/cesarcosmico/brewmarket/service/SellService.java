package com.cesarcosmico.brewmarket.service;

import com.cesarcosmico.brewmarket.config.MarketConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellService {

    private final BrewPriceService priceService;
    private final EconomyService economyService;

    public SellService(BrewPriceService priceService, EconomyService economyService) {
        this.priceService = priceService;
        this.economyService = economyService;
    }

    public record SellResult(double money, int itemCount) {
    }

    public double calculateValue(Inventory inventory, MarketConfig config) {
        double total = 0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
            }
        }
        return total;
    }

    public double calculateInventoryValue(Player player) {
        double total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
            }
        }
        return total;
    }

    public double calculateTotalValue(Inventory inventory, MarketConfig config, Player player) {
        return calculateValue(inventory, config) + calculateInventoryValue(player);
    }

    public int countBrews(Inventory inventory, MarketConfig config) {
        int count = 0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && priceService.evaluate(item).isPresent()) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public int countBrewsInPlayerInventory(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && priceService.evaluate(item).isPresent()) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public SellResult sellFromGui(Inventory inventory, MarketConfig config) {
        double total = 0;
        int count = 0;

        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
                count += item.getAmount();
                inventory.setItem(slot, null);
            }
        }

        return new SellResult(total, count);
    }

    public SellResult sellAll(Inventory inventory, MarketConfig config, Player player) {
        SellResult guiResult = sellFromGui(inventory, config);

        double inventoryTotal = 0;
        int inventoryCount = 0;

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                inventoryTotal += eval.get().price() * item.getAmount();
                inventoryCount += item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }

        return new SellResult(
                guiResult.money() + inventoryTotal,
                guiResult.itemCount() + inventoryCount
        );
    }

    public boolean deposit(Player player, double amount) {
        return economyService.deposit(player, amount);
    }

    public String format(double amount) {
        return economyService.format(amount);
    }
}