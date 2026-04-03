package com.cesarcosmico.brewmarket.command.feature;

import com.cesarcosmico.brewmarket.config.LangConfig;
import com.cesarcosmico.brewmarket.config.MarketConfig;
import com.cesarcosmico.brewmarket.gui.BrewMarketGUI;
import com.cesarcosmico.brewmarket.service.SellService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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

        BrewMarketGUI gui = new BrewMarketGUI(config, sellServiceSupplier.get(), player);
        gui.open(player);
        return Command.SINGLE_SUCCESS;
    }
}