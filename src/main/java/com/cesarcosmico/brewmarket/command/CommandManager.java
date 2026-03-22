package com.cesarcosmico.brewmarket.command;

import com.cesarcosmico.brewmarket.config.LangConfig;
import com.cesarcosmico.brewmarket.config.MarketConfig;
import com.cesarcosmico.brewmarket.gui.BrewMarketGUI;
import com.cesarcosmico.brewmarket.service.SellService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class CommandManager {

    private final Supplier<MarketConfig> configSupplier;
    private final Supplier<SellService> sellServiceSupplier;
    private final Supplier<LangConfig> langSupplier;
    private final Runnable reloadAction;

    public CommandManager(
            Supplier<MarketConfig> configSupplier,
            Supplier<SellService> sellServiceSupplier,
            Supplier<LangConfig> langSupplier,
            Runnable reloadAction
    ) {
        this.configSupplier = configSupplier;
        this.sellServiceSupplier = sellServiceSupplier;
        this.langSupplier = langSupplier;
        this.reloadAction = reloadAction;
    }

    public LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("brewmarket")
                .requires(source -> source.getSender().hasPermission("brewmarket.use"))
                .executes(this::openMarket)
                .then(createReloadCommand())
                .build();
    }

    public LiteralCommandNode<CommandSourceStack> createAliasCommand() {
        return Commands.literal("bm")
                .requires(source -> source.getSender().hasPermission("brewmarket.use"))
                .executes(this::openMarket)
                .then(createReloadCommand())
                .build();
    }

    private LiteralCommandNode<CommandSourceStack> createReloadCommand() {
        return Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("brewmarket.admin"))
                .executes(this::reloadConfig)
                .build();
    }

    private int openMarket(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            langSupplier.get().send(context.getSource().getSender(), "command.only-players");
            return Command.SINGLE_SUCCESS;
        }

        BrewMarketGUI gui = new BrewMarketGUI(
                configSupplier.get(),
                sellServiceSupplier.get(),
                player
        );
        gui.open(player);
        return Command.SINGLE_SUCCESS;
    }

    private int reloadConfig(CommandContext<CommandSourceStack> context) {
        reloadAction.run();
        langSupplier.get().send(context.getSource().getSender(), "command.reload-success");
        return Command.SINGLE_SUCCESS;
    }
}