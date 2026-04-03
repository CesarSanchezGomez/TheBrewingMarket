package com.cesarcosmico.brewmarket.command;

import com.cesarcosmico.brewmarket.command.feature.HistoryCommand;
import com.cesarcosmico.brewmarket.command.feature.MarketCommand;
import com.cesarcosmico.brewmarket.command.feature.ReloadCommand;
import com.cesarcosmico.brewmarket.config.LangConfig;
import com.cesarcosmico.brewmarket.config.MarketConfig;
import com.cesarcosmico.brewmarket.service.SellService;
import com.cesarcosmico.brewmarket.storage.SellHistoryService;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public class CommandManager {

    private final MarketCommand marketCommand;
    private final ReloadCommand reloadCommand;
    private final HistoryCommand historyCommand;

    public CommandManager(Supplier<MarketConfig> configSupplier,
                          Supplier<SellService> sellServiceSupplier,
                          Supplier<LangConfig> langSupplier,
                          Supplier<SellHistoryService> historySupplier,
                          JavaPlugin plugin,
                          Runnable reloadAction) {
        this.marketCommand = new MarketCommand(configSupplier, sellServiceSupplier, langSupplier, plugin);
        this.reloadCommand = new ReloadCommand(langSupplier, reloadAction);
        this.historyCommand = new HistoryCommand(
                configSupplier, sellServiceSupplier, langSupplier, historySupplier, plugin);
    }

    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("brewmarket")
                .requires(source -> source.getSender().hasPermission("brewmarket.use"))
                .executes(marketCommand::execute)
                .then(reloadCommand.create())
                .then(historyCommand.create())
                .build();
    }

    public LiteralCommandNode<CommandSourceStack> createAliasCommand() {
        return Commands.literal("bm")
                .requires(source -> source.getSender().hasPermission("brewmarket.use"))
                .executes(marketCommand::execute)
                .then(reloadCommand.create())
                .then(historyCommand.create())
                .build();
    }
}