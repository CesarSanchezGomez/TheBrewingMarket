package com.cesarcosmico.thebrewingmarket.listener;

import com.cesarcosmico.thebrewingmarket.config.IconConfig;
import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.gui.TheBrewingMarketGUI;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class MarketGUIListener implements Listener {

    private final JavaPlugin plugin;
    private final LangConfig langConfig;
    private final SellHistoryService historyService;
    private final Logger logger;

    public MarketGUIListener(JavaPlugin plugin, LangConfig langConfig,
                             SellHistoryService historyService) {
        this.plugin = plugin;
        this.langConfig = langConfig;
        this.historyService = historyService;
        this.logger = plugin.getLogger();
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

        double previewValue = sellService.calculateValue(
                gui.getInventory(), config, gui.isShulkerEnabled());

        executeSell(gui, player, previewValue,
                () -> sellService.detailedSellFromGui(gui.getInventory(), config, gui.isShulkerEnabled()),
                config.getSellAllow(), config.getSellDeny());
    }

    private void handleSellAll(TheBrewingMarketGUI gui, Player player) {
        SellService sellService = gui.getSellService();
        MarketConfig config = gui.getConfig();

        double previewValue = sellService.calculateTotalValue(
                gui.getInventory(), config, player, gui.isShulkerEnabled());

        executeSell(gui, player, previewValue,
                () -> sellService.detailedSellAll(gui.getInventory(), config, player, gui.isShulkerEnabled()),
                config.getSellAllAllow(), config.getSellAllDeny());
    }

    private void executeSell(TheBrewingMarketGUI gui, Player player, double previewValue,
                             java.util.function.Supplier<SellService.DetailedSellResult> sellAction,
                             IconConfig allowSound, IconConfig denySound) {
        MarketConfig config = gui.getConfig();
        SellService sellService = gui.getSellService();

        if (previewValue <= 0) {
            langConfig.send(player, "market.sell-nothing");
            playSound(player, config.getActionSound(denySound));
            return;
        }

        SellService.DetailedSellResult result = sellAction.get();

        if (result.money() > 0 && sellService.deposit(player, result.money())) {
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
                        d.recipeName(),
                        d.displayName() != null ? mini.serialize(d.displayName()) : d.recipeName(),
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