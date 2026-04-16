package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.util.ShulkerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SellService {

    private final BrewEvaluator brewEvaluator;
    private final EconomyService economyService;

    public SellService(BrewEvaluator brewEvaluator, EconomyService economyService) {
        this.brewEvaluator = brewEvaluator;
        this.economyService = economyService;
    }

    public record SoldBrewDetail(String recipeId, double score, double pricePerUnit, int quantity,
                                 Component displayName) {
        public double total() {
            return pricePerUnit * score * quantity;
        }
    }

    public record DetailedSellResult(double money, int itemCount, List<SoldBrewDetail> details) {
    }

    public record InventoryStats(double value, int brewCount) {
        public static final InventoryStats EMPTY = new InventoryStats(0.0, 0);
    }

    public record SellPlan(double money,
                           int itemCount,
                           List<SoldBrewDetail> details,
                           Runnable applyAction) {
        public static SellPlan empty() {
            return new SellPlan(0.0, 0, List.of(), () -> {});
        }

        public boolean hasValue() {
            return money > 0 && itemCount > 0;
        }

        public void apply() {
            applyAction.run();
        }

        public DetailedSellResult toDetailedResult() {
            return new DetailedSellResult(money, itemCount, details);
        }
    }

    // ── Single-pass inventory stats (used by GUI refresh) ────────

    public InventoryStats computeGuiStats(Inventory inventory, MarketConfig config, boolean includeShulkers) {
        double value = 0;
        int count = 0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            var eval = brewEvaluator.evaluate(item);
            if (eval.isPresent()) {
                int amount = item.getAmount();
                value += eval.get().price() * amount;
                count += amount;
            } else if (includeShulkers && ShulkerUtil.isShulkerBoxMaterial(item)) {
                InventoryStats sub = readShulkerStats(item);
                value += sub.value();
                count += sub.brewCount();
            }
        }
        return new InventoryStats(value, count);
    }

    public InventoryStats computePlayerStats(Player player, boolean includeShulkers) {
        double value = 0;
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;

            var eval = brewEvaluator.evaluate(item);
            if (eval.isPresent()) {
                int amount = item.getAmount();
                value += eval.get().price() * amount;
                count += amount;
            } else if (includeShulkers && ShulkerUtil.isShulkerBoxMaterial(item)) {
                InventoryStats sub = readShulkerStats(item);
                value += sub.value();
                count += sub.brewCount();
            }
        }
        return new InventoryStats(value, count);
    }

    // ── Sell plans (used by listener for Sell / Sell All) ────────

    public SellPlan planSellFromGui(Inventory inventory, MarketConfig config, boolean includeShulkers) {
        Map<String, SoldBrewDetail> detailMap = new LinkedHashMap<>();
        List<Runnable> commits = new ArrayList<>();
        double total = 0;
        int count = 0;

        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            var eval = brewEvaluator.evaluate(item);
            if (eval.isPresent()) {
                int amount = item.getAmount();
                total += eval.get().price() * amount;
                count += amount;
                accumulateDetail(detailMap, eval.get(), amount);
                final int capturedSlot = slot;
                commits.add(() -> inventory.setItem(capturedSlot, null));
            } else if (includeShulkers && ShulkerUtil.isShulkerBoxMaterial(item)) {
                ShulkerPlan sp = planShulkerSell(item, detailMap);
                if (sp != null) {
                    total += sp.value;
                    count += sp.count;
                    commits.add(sp.commit);
                }
            }
        }

        if (total <= 0) return SellPlan.empty();
        return new SellPlan(total, count, new ArrayList<>(detailMap.values()),
                () -> commits.forEach(Runnable::run));
    }

    public SellPlan planSellAll(Inventory inventory, MarketConfig config, Player player, boolean includeShulkers) {
        Map<String, SoldBrewDetail> detailMap = new LinkedHashMap<>();
        List<Runnable> commits = new ArrayList<>();
        double total = 0;
        int count = 0;

        // GUI slots
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;

            var eval = brewEvaluator.evaluate(item);
            if (eval.isPresent()) {
                int amount = item.getAmount();
                total += eval.get().price() * amount;
                count += amount;
                accumulateDetail(detailMap, eval.get(), amount);
                final int capturedSlot = slot;
                commits.add(() -> inventory.setItem(capturedSlot, null));
            } else if (includeShulkers && ShulkerUtil.isShulkerBoxMaterial(item)) {
                ShulkerPlan sp = planShulkerSell(item, detailMap);
                if (sp != null) {
                    total += sp.value;
                    count += sp.count;
                    commits.add(sp.commit);
                }
            }
        }

        // Player inventory
        PlayerInventory playerInv = player.getInventory();
        ItemStack[] contents = playerInv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) continue;

            var eval = brewEvaluator.evaluate(item);
            if (eval.isPresent()) {
                int amount = item.getAmount();
                total += eval.get().price() * amount;
                count += amount;
                accumulateDetail(detailMap, eval.get(), amount);
                final int capturedIndex = i;
                commits.add(() -> playerInv.setItem(capturedIndex, null));
            } else if (includeShulkers && ShulkerUtil.isShulkerBoxMaterial(item)) {
                ShulkerPlan sp = planShulkerSell(item, detailMap);
                if (sp != null) {
                    total += sp.value;
                    count += sp.count;
                    commits.add(sp.commit);
                }
            }
        }

        if (total <= 0) return SellPlan.empty();
        return new SellPlan(total, count, new ArrayList<>(detailMap.values()),
                () -> commits.forEach(Runnable::run));
    }

    // ── Economy delegation ───────────────────────────────────────

    public boolean deposit(Player player, double amount) {
        return economyService.deposit(player, amount);
    }

    public String format(double amount) {
        return economyService.format(amount);
    }

    // ── Shulker helpers ──────────────────────────────────────────

    private InventoryStats readShulkerStats(ItemStack shulkerItem) {
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return InventoryStats.EMPTY;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return InventoryStats.EMPTY;

        double value = 0;
        int count = 0;
        for (ItemStack inner : shulker.getInventory().getContents()) {
            if (inner == null || inner.getType().isAir()) continue;
            var eval = brewEvaluator.evaluate(inner);
            if (eval.isPresent()) {
                value += eval.get().price() * inner.getAmount();
                count += inner.getAmount();
            }
        }
        return new InventoryStats(value, count);
    }

    private ShulkerPlan planShulkerSell(ItemStack shulkerItem, Map<String, SoldBrewDetail> detailMap) {
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta bsm)) return null;
        if (!(bsm.getBlockState() instanceof ShulkerBox shulker)) return null;

        Inventory inv = shulker.getInventory();
        double value = 0;
        int count = 0;
        boolean anyFound = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack inner = inv.getItem(i);
            if (inner == null || inner.getType().isAir()) continue;
            var eval = brewEvaluator.evaluate(inner);
            if (eval.isPresent()) {
                value += eval.get().price() * inner.getAmount();
                count += inner.getAmount();
                accumulateDetail(detailMap, eval.get(), inner.getAmount());
                inv.setItem(i, null);
                anyFound = true;
            }
        }

        if (!anyFound) return null;

        return new ShulkerPlan(value, count, () -> {
            bsm.setBlockState(shulker);
            shulkerItem.setItemMeta(bsm);
        });
    }

    private record ShulkerPlan(double value, int count, Runnable commit) {
    }

    // ── Detail accumulation ──────────────────────────────────────

    private void accumulateDetail(Map<String, SoldBrewDetail> map,
                                  BrewEvaluator.BrewEvaluation eval, int amount) {
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
