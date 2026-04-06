package com.cesarcosmico.thebrewingmarket.command;

import com.cesarcosmico.thebrewingmarket.command.feature.HistoryCommand;
import com.cesarcosmico.thebrewingmarket.command.feature.MarketCommand;
import com.cesarcosmico.thebrewingmarket.command.feature.ReloadCommand;
import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public final class CommandManager {

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
        return buildCommandTree("thebrewingmarket");
    }

    public LiteralCommandNode<CommandSourceStack> createAliasCommand() {
        return buildCommandTree("tbm");
    }

    private LiteralCommandNode<CommandSourceStack> buildCommandTree(String name) {
        return Commands.literal(name)
                .requires(source -> source.getSender().hasPermission("thebrewingmarket.use"))
                .executes(marketCommand::execute)
                .then(reloadCommand.create())
                .then(historyCommand.create())
                .build();
    }
}
