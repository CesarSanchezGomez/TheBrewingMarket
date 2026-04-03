package com.cesarcosmico.brewmarket.command.feature;

import com.cesarcosmico.brewmarket.config.LangConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Supplier;

public class ReloadCommand {

    private final Supplier<LangConfig> langSupplier;
    private final Runnable reloadAction;

    public ReloadCommand(Supplier<LangConfig> langSupplier, Runnable reloadAction) {
        this.langSupplier = langSupplier;
        this.reloadAction = reloadAction;
    }

    public LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("brewmarket.admin"))
                .executes(this::execute)
                .build();
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        reloadAction.run();
        langSupplier.get().send(context.getSource().getSender(), "command.reload-success");
        return Command.SINGLE_SUCCESS;
    }
}