package com.cesarcosmico.thebrewingmarket.listener;

import com.cesarcosmico.thebrewingmarket.config.IconConfig;
import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig.LimitationConfig;
import com.cesarcosmico.thebrewingmarket.gui.TheBrewingMarketGUI;
import com.cesarcosmico.thebrewingmarket.service.DailyEarningsTracker;
import com.cesarcosmico.thebrewingmarket.service.PlayerStatsCache;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class MarketGUIListener implements Listener {

    private final JavaPlugin plugin;
    private final LangConfig langConfig;
    private final SellHistoryService historyService;
    private final DailyEarningsTracker earningsTracker;
    private final PlayerStatsCache playerStatsCache;
    private final Supplier<MarketConfig> marketConfigSupplier;
    private final Logger logger;

    public MarketGUIListener(JavaPlugin plugin, LangConfig langConfig,
                             SellHistoryService historyService,
                             DailyEarningsTracker earningsTracker,
                             PlayerStatsCache playerStatsCache,
                             Supplier<MarketConfig> marketConfigSupplier) {
        this.plugin = plugin;
        this.langConfig = langConfig;
        this.historyService = historyService;
        this.earningsTracker = earningsTracker;
        this.playerStatsCache = playerStatsCache;
        this.marketConfigSupplier = marketConfigSupplier;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        earningsTracker.seedAsync(event.getPlayer().getUniqueId());
        playerStatsCache.seedAsync(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        earningsTracker.invalidate(event.getPlayer().getUniqueId());
        playerStatsCache.invalidate(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TheBrewingMarketGUI gui)) return;

        Player player = (Player) event.getWhoClicked();
        MarketConfig config = gui.getConfig();
        int rawSlot = event.getRawSlot();

        if (rawSlot >= gui.getInventory().getSize()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                handleShiftClickFromPlayer(gui, event);
                scheduleRefresh(gui);
            }
            return;
        }

        if (config.isItemSlot(rawSlot)) {
            scheduleRefresh(gui);
            return;
        }

        event.setCancelled(true);

        if (config.isSellSlot(rawSlot)) {
            handleSell(gui, player);
        } else if (config.isSellAllSlot(rawSlot)) {
            handleSellAll(gui, player);
        } else if (config.isCloseSlot(rawSlot)) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof TheBrewingMarketGUI gui)) return;

        MarketConfig config = gui.getConfig();

        for (int slot : event.getRawSlots()) {
            if (slot < gui.getInventory().getSize() && !config.isItemSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }

        scheduleRefresh(gui);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TheBrewingMarketGUI gui)) return;
        gui.stopAutoRefresh();
        gui.returnItems((Player) event.getPlayer());
    }

    private void handleShiftClickFromPlayer(TheBrewingMarketGUI gui, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        MarketConfig config = gui.getConfig();

        for (int itemSlot : config.getItemSlots().stream().sorted().toList()) {
            ItemStack existing = gui.getInventory().getItem(itemSlot);
            if (existing == null || existing.getType().isAir()) {
                gui.getInventory().setItem(itemSlot, clicked.clone());
                event.setCurrentItem(null);
                return;
            }
        }
    }

    private void handleSell(TheBrewingMarketGUI gui, Player player) {
        SellService sellService = gui.getSellService();
        MarketConfig config = gui.getConfig();

        SellService.SellPlan plan = sellService.planSellFromGui(
                gui.getInventory(), config, gui.isShulkerEnabled());

        executeSell(gui, player, plan, config.getSellAllow(), config.getSellDeny());
    }

    private void handleSellAll(TheBrewingMarketGUI gui, Player player) {
        SellService sellService = gui.getSellService();
        MarketConfig config = gui.getConfig();

        SellService.SellPlan plan = sellService.planSellAll(
                gui.getInventory(), config, player, gui.isShulkerEnabled());

        executeSell(gui, player, plan, config.getSellAllAllow(), config.getSellAllDeny());
    }

    private void executeSell(TheBrewingMarketGUI gui, Player player, SellService.SellPlan plan,
                             IconConfig allowSound, IconConfig denySound) {
        MarketConfig config = gui.getConfig();
        SellService sellService = gui.getSellService();

        if (!plan.hasValue()) {
            langConfig.send(player, "market.sell-nothing");
            playSound(player, config.getActionSound(denySound));
            return;
        }

        LimitationConfig limit = marketConfigSupplier.get().getLimitation();
        if (limit.active()) {
            double earned = earningsTracker.getTodayEarnings(player.getUniqueId());
            double remaining = Math.max(0, limit.earnings() - earned);
            if (remaining <= 0 || plan.money() > remaining) {
                langConfig.send(player, "market.limit-reached",
                        "{earned}", sellService.format(earned),
                        "{limit}", sellService.format(limit.earnings()),
                        "{remaining}", sellService.format(remaining));
                playSound(player, config.getActionSound(denySound));
                return;
            }
        }

        plan.apply();
        SellService.DetailedSellResult result = plan.toDetailedResult();

        if (result.money() > 0 && sellService.deposit(player, result.money())) {
            earningsTracker.record(player.getUniqueId(), result.money());
            playerStatsCache.recordSale(player.getUniqueId(), result);
            String money = sellService.format(result.money());
            langConfig.send(player, "market.sell-success",
                    "{money}", money,
                    "{sold_amount}", String.valueOf(result.itemCount()));
            playSound(player, config.getActionSound(allowSound));
            logHistory(player, result);
        } else {
            langConfig.send(player, "market.sell-error");
            playSound(player, config.getActionSound(denySound));
        }

        gui.refreshSellButtons();
    }

    private void logHistory(Player player, SellService.DetailedSellResult result) {
        if (historyService == null || result.details().isEmpty()) return;

        MiniMessage mini = MiniMessage.miniMessage();
        List<SellHistoryService.SellEntry> entries = result.details().stream()
                .map(d -> new SellHistoryService.SellEntry(
                        d.recipeId(),
                        d.displayName() != null ? mini.serialize(d.displayName()) : d.recipeId(),
                        d.score(), d.pricePerUnit(), d.quantity(), d.total()))
                .toList();

        historyService.logEntries(player.getUniqueId(), player.getName(), entries)
                .exceptionally(ex -> {
                    logger.warning("Failed to log sell history: " + ex.getMessage());
                    return null;
                });
    }

    private void playSound(Player player, String soundKey) {
        if (soundKey == null || soundKey.isEmpty()) return;
        try {
            player.playSound(player.getLocation(), soundKey, 1.0f, 1.0f);
        } catch (Exception e) {
            logger.warning("Invalid sound: " + soundKey);
        }
    }

    private void scheduleRefresh(TheBrewingMarketGUI gui) {
        plugin.getServer().getScheduler().runTask(plugin, gui::refreshSellButtons);
    }
}