package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.util.ShulkerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SellService {

    private final BrewingPriceService priceService;
    private final EconomyService economyService;

    public SellService(BrewingPriceService priceService, EconomyService economyService) {
        this.priceService = priceService;
        this.economyService = economyService;
    }

    public record SellResult(double money, int itemCount) {
    }

    public record SoldBrewDetail(String recipeId, double score, double pricePerUnit, int quantity,
                                 Component displayName) {
        public double total() {
            return pricePerUnit * score * quantity;
        }
    }

    public record DetailedSellResult(double money, int itemCount, List<SoldBrewDetail> details) {
        public SellResult toSimple() {
            return new SellResult(money, itemCount);
        }
    }

    // ── Value calculation ────────────────────────────────────────

    public double calculateValue(Inventory inventory, MarketConfig config, boolean includeShulkers) {
        double total = 0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
            } else if (includeShulkers && item != null && ShulkerUtil.isShulkerBox(item)) {
                total += calculateShulkerValue(item);
            }
        }
        return total;
    }

    public double calculateInventoryValue(Player player, boolean includeShulkers) {
        double total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
            } else if (includeShulkers && ShulkerUtil.isShulkerBox(item)) {
                total += calculateShulkerValue(item);
            }
        }
        return total;
    }

    public double calculateTotalValue(Inventory inventory, MarketConfig config,
                                      Player player, boolean includeShulkers) {
        return calculateValue(inventory, config, includeShulkers) + calculateInventoryValue(player, includeShulkers);
    }

    // ── Brew counting ────────────────────────────────────────────

    public int countBrews(Inventory inventory, MarketConfig config, boolean includeShulkers) {
        int count = 0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && priceService.evaluate(item).isPresent()) {
                count += item.getAmount();
            } else if (includeShulkers && item != null && ShulkerUtil.isShulkerBox(item)) {
                count += countBrewsInShulker(item);
            }
        }
        return count;
    }

    public int countBrewsInPlayerInventory(Player player, boolean includeShulkers) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (priceService.evaluate(item).isPresent()) {
                count += item.getAmount();
            } else if (includeShulkers && ShulkerUtil.isShulkerBox(item)) {
                count += countBrewsInShulker(item);
            }
        }
        return count;
    }

    // ── Detailed sell (with per-recipe breakdown) ────────────────

    public DetailedSellResult detailedSellFromGui(Inventory inventory, MarketConfig config,
                                                  boolean includeShulkers) {
        double total = 0;
        int count = 0;
        Map<String, SoldBrewDetail> detailMap = new LinkedHashMap<>();

        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                double itemTotal = eval.get().price() * item.getAmount();
                total += itemTotal;
                count += item.getAmount();
                accumulateDetail(detailMap, eval.get(), item.getAmount());
                inventory.setItem(slot, null);
            } else if (includeShulkers && item != null && ShulkerUtil.isShulkerBox(item)) {
                SellResult shulkerResult = processShulker(item, detailMap);
                total += shulkerResult.money();
                count += shulkerResult.itemCount();
            }
        }

        return new DetailedSellResult(total, count, new ArrayList<>(detailMap.values()));
    }

    public DetailedSellResult detailedSellAll(Inventory inventory, MarketConfig config,
                                              Player player, boolean includeShulkers) {
        DetailedSellResult guiResult = detailedSellFromGui(inventory, config, includeShulkers);

        double inventoryTotal = 0;
        int inventoryCount = 0;
        Map<String, SoldBrewDetail> detailMap = new LinkedHashMap<>();
        guiResult.details().forEach(d -> detailMap.put(detailKey(d.recipeId(), d.score()), d));

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;

            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                inventoryTotal += eval.get().price() * item.getAmount();
                inventoryCount += item.getAmount();
                accumulateDetail(detailMap, eval.get(), item.getAmount());
                player.getInventory().setItem(i, null);
            } else if (includeShulkers && ShulkerUtil.isShulkerBox(item)) {
                SellResult shulkerResult = processShulker(item, detailMap);
                inventoryTotal += shulkerResult.money();
                inventoryCount += shulkerResult.itemCount();
            }
        }

        return new DetailedSellResult(
                guiResult.money() + inventoryTotal,
                guiResult.itemCount() + inventoryCount,
                new ArrayList<>(detailMap.values())
        );
    }

    // ── Economy delegation ───────────────────────────────────────

    public boolean deposit(Player player, double amount) {
        return economyService.deposit(player, amount);
    }

    public String format(double amount) {
        return economyService.format(amount);
    }

    // ── Shulker helpers (private) ────────────────────────────────

    private Optional<ShulkerBox> openShulker(ItemStack shulkerItem) {
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return Optional.empty();
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return Optional.empty();
        return Optional.of(shulker);
    }

    private double calculateShulkerValue(ItemStack shulkerItem) {
        return openShulker(shulkerItem).map(shulker -> {
            double total = 0;
            for (ItemStack item : shulker.getInventory().getContents()) {
                if (item == null) continue;
                var eval = priceService.evaluate(item);
                if (eval.isPresent()) {
                    total += eval.get().price() * item.getAmount();
                }
            }
            return total;
        }).orElse(0.0);
    }

    private int countBrewsInShulker(ItemStack shulkerItem) {
        return openShulker(shulkerItem).map(shulker -> {
            int count = 0;
            for (ItemStack item : shulker.getInventory().getContents()) {
                if (item == null) continue;
                if (priceService.evaluate(item).isPresent()) {
                    count += item.getAmount();
                }
            }
            return count;
        }).orElse(0);
    }

    private SellResult processShulker(ItemStack shulkerItem, Map<String, SoldBrewDetail> detailMap) {
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return new SellResult(0, 0);
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return new SellResult(0, 0);

        double total = 0;
        int count = 0;
        Inventory inv = shulker.getInventory();
        boolean anyProcessed = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            var eval = priceService.evaluate(item);
            if (eval.isPresent()) {
                total += eval.get().price() * item.getAmount();
                count += item.getAmount();
                accumulateDetail(detailMap, eval.get(), item.getAmount());
                inv.setItem(i, null);
                anyProcessed = true;
            }
        }

        if (anyProcessed) {
            bsm.setBlockState(shulker);
            shulkerItem.setItemMeta(bsm);
        }

        return new SellResult(total, count);
    }

    // ── Detail accumulation ──────────────────────────────────────

    private void accumulateDetail(Map<String, SoldBrewDetail> map,
                                  BrewingPriceService.BrewEvaluation eval, int amount) {
        String key = detailKey(eval.recipeId(), eval.score());
        map.merge(key, new SoldBrewDetail(eval.recipeId(), eval.score(),
                        eval.price() / eval.score(), amount, eval.displayName()),
                (existing, incoming) -> new SoldBrewDetail(
                        existing.recipeId(), existing.score(), existing.pricePerUnit(),
                        existing.quantity() + incoming.quantity(), existing.displayName()
                ));
    }

    private static String detailKey(final String recipeId, final double score) {
        return recipeId + ":" + score;
    }
}