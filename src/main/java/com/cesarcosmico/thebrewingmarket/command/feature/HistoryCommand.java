package com.cesarcosmico.thebrewingmarket.command.feature;

import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.util.TimeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class HistoryCommand {

    private static final String[] TIME_SUGGESTIONS = {"1s", "1m", "1h", "1d", "1w", "1M", "1Y"};

    private final Supplier<MarketConfig> configSupplier;
    private final Supplier<SellService> sellServiceSupplier;
    private final Supplier<LangConfig> langSupplier;
    private final Supplier<SellHistoryService> historySupplier;
    private final JavaPlugin plugin;

    public HistoryCommand(Supplier<MarketConfig> configSupplier,
                          Supplier<SellService> sellServiceSupplier,
                          Supplier<LangConfig> langSupplier,
                          Supplier<SellHistoryService> historySupplier,
                          JavaPlugin plugin) {
        this.configSupplier = configSupplier;
        this.sellServiceSupplier = sellServiceSupplier;
        this.langSupplier = langSupplier;
        this.historySupplier = historySupplier;
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> create() {
        return Commands.literal("history")
                .requires(source -> source.getSender().hasPermission("thebrewingmarket.history"))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestOnlinePlayers)
                        .then(Commands.argument("time", StringArgumentType.word())
                                .suggests(this::suggestTimeRanges)
                                .executes(ctx -> execute(ctx, 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "page")))
                                )
                        )
                )
                .build();
    }

    private CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(input)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTimeRanges(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (String suggestion : TIME_SUGGESTIONS) {
            if (suggestion.toLowerCase().startsWith(input)) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    @SuppressWarnings("deprecation")
    private int execute(CommandContext<CommandSourceStack> context, int page) {
        SellHistoryService historyService = historySupplier.get();
        if (historyService == null) return Command.SINGLE_SUCCESS;

        String targetName = StringArgumentType.getString(context, "player");
        String timeArg = StringArgumentType.getString(context, "time");
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        long since;
        try {
            since = TimeUtil.parseTimeRange(timeArg);
        } catch (IllegalArgumentException e) {
            langSupplier.get().send(context.getSource().getSender(), "history.invalid-time");
            return Command.SINGLE_SUCCESS;
        }

        int recordsPerPage = configSupplier.get().getHistoryPerPage();

        historyService.countHistory(target.getUniqueId(), since).thenCompose(totalCount -> {
            int maxPage = Math.max(1, (int) Math.ceil((double) totalCount / recordsPerPage));
            int safePage = Math.min(page, maxPage);
            int offset = (safePage - 1) * recordsPerPage;

            return historyService.getHistory(target.getUniqueId(), since, recordsPerPage, offset)
                    .thenAccept(records ->
                            plugin.getServer().getScheduler().runTask(plugin, () ->
                                    renderHistory(context, records, targetName, timeArg, safePage, maxPage))
                    );
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Failed to retrieve sell history: " + ex.getMessage());
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private void renderHistory(CommandContext<CommandSourceStack> context,
                               List<SellHistoryService.SellRecord> records,
                               String targetName, String timeArg, int page, int maxPage) {
        LangConfig lang = langSupplier.get();

        if (records.isEmpty()) {
            lang.send(context.getSource().getSender(), "history.empty",
                    "{player}", targetName);
            return;
        }

        SellService sellService = sellServiceSupplier.get();
        List<Component> entryComponents = new ArrayList<>();

        for (SellHistoryService.SellRecord record : records) {
            String headTag = isBedrockPlayer(record.playerUuid())
                    ? "<head:" + record.playerName() + ">"
                    : "<head:" + record.playerUuid() + ">";

            Component entryComponent = lang.get("history.entry",
                    "{player_head}", headTag,
                    "{recipe}", record.displayName(),
                    "{quantity}", String.valueOf(record.quantity()),
                    "{quality}", String.format("%.0f%%", record.quality() * 100),
                    "{total}", sellService.format(record.total())
            );

            Component hoverComponent = lang.get("history.entry_hover",
                    "{time_ago}", TimeUtil.relativeTime(record.soldAt()),
                    "{exact_date}", TimeUtil.exactTime(record.soldAt())
            );

            entryComponents.add(entryComponent
                    .hoverEvent(HoverEvent.showText(hoverComponent)));
        }

        Component entriesBlock = Component.join(JoinConfiguration.newlines(), entryComponents);
        Component previousPage = buildPreviousPage(lang, targetName, timeArg, page);
        Component nextPage = buildNextPage(lang, targetName, timeArg, page, maxPage);

        TagResolver componentResolver = TagResolver.resolver(
                Placeholder.component("entries", entriesBlock),
                Placeholder.component("previous_page", previousPage),
                Placeholder.component("next_page", nextPage)
        );

        lang.send(context.getSource().getSender(),
                "history.display", componentResolver,
                new String[]{"entries", "previous_page", "next_page"},
                "{player}", targetName,
                "{page}", String.format("%02d", page),
                "{max_page}", String.format("%02d", maxPage));
    }

    private Component buildPreviousPage(LangConfig lang, String targetName, String timeArg, int currentPage) {
        if (currentPage > 1) {
            Component text = lang.get("history.navigation.previous");
            Component hover = lang.get("history.navigation.previous_hover",
                    "{page}", String.valueOf(currentPage - 1));
            return text
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.runCommand("/tbm history " + targetName + " " + timeArg + " " + (currentPage - 1)));
        }
        return lang.get("history.navigation.previous_disabled");
    }

    private Component buildNextPage(LangConfig lang, String targetName, String timeArg, int currentPage, int maxPage) {
        if (currentPage < maxPage) {
            Component text = lang.get("history.navigation.next");
            Component hover = lang.get("history.navigation.next_hover",
                    "{page}", String.valueOf(currentPage + 1));
            return text
                    .hoverEvent(HoverEvent.showText(hover))
                    .clickEvent(ClickEvent.runCommand("/tbm history " + targetName + " " + timeArg + " " + (currentPage + 1)));
        }
        return lang.get("history.navigation.next_disabled");
    }

    private boolean isBedrockPlayer(UUID uuid) {
        return uuid.toString().startsWith("00000000-0000-0000");
    }
}
