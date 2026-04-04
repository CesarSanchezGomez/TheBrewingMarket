package com.cesarcosmico.thebrewingmarket.command.feature;

import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.gui.TheBrewingMarketGUI;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public class MarketCommand {

    private final Supplier<MarketConfig> configSupplier;
    private final Supplier<SellService> sellServiceSupplier;
    private final Supplier<LangConfig> langSupplier;
    private final JavaPlugin plugin;

    public MarketCommand(Supplier<MarketConfig> configSupplier,
                         Supplier<SellService> sellServiceSupplier,
                         Supplier<LangConfig> langSupplier,
                         JavaPlugin plugin) {
        this.configSupplier = configSupplier;
        this.sellServiceSupplier = sellServiceSupplier;
        this.langSupplier = langSupplier;
        this.plugin = plugin;
    }

    public int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            langSupplier.get().send(context.getSource().getSender(), "command.only-players");
            return Command.SINGLE_SUCCESS;
        }

        MarketConfig config = configSupplier.get();
        boolean shulkerEnabled = config.isShulkerSellingEnabled()
                && player.hasPermission("thebrewingmarket.shulker");

        TheBrewingMarketGUI gui = new TheBrewingMarketGUI(config, sellServiceSupplier.get(), player, shulkerEnabled);
        gui.open(player);
        gui.startAutoRefresh(plugin);
        return Command.SINGLE_SUCCESS;
    }
}