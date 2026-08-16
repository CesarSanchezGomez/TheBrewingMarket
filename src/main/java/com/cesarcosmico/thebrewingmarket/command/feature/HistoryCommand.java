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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class HistoryCommand {

    private static final String[] TIME_SUGGESTIONS = {"1s", "1m", "1h", "1d", "1w", "1M", "1Y"};
    private static final String DEFAULT_HEAD_UUID = "8667ba71-b85a-4004-af54-457a9734eed7";

    private final Supplier<MarketConfig> configSupplier;
    private final Supplier<SellService> sellServiceSupplier;
    private final Supplier<LangConfig> langSupplier;
    private final Supplier<SellHistoryService> historySupplier;
    private final JavaPlugin plugin;

    public HistoryCommand(final Supplier<MarketConfig> configSupplier,
                          final Supplier<SellService> sellServiceSupplier,
                          final Supplier<LangConfig> langSupplier,
                          final Supplier<SellHistoryService> historySupplier,
                          final JavaPlugin plugin) {
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
            final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        final String input = builder.getRemaining().toLowerCase();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(input)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestTimeRanges(
            final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        final String input = builder.getRemaining().toLowerCase();
        for (final String suggestion : TIME_SUGGESTIONS) {
            if (suggestion.toLowerCase().startsWith(input)) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

    private int execute(final CommandContext<CommandSourceStack> context, final int page) {
        final SellHistoryService historyService = historySupplier.get();
        if (historyService == null) return Command.SINGLE_SUCCESS;

        final String targetName = StringArgumentType.getString(context, "player");
        final String timeArg = StringArgumentType.getString(context, "time");

        final long since;
        try {
            since = TimeUtil.parseTimeRange(timeArg);
        } catch (IllegalArgumentException e) {
            langSupplier.get().send(context.getSource().getSender(), "history.invalid-time");
            return Command.SINGLE_SUCCESS;
        }

        final int recordsPerPage = configSupplier.get().getHistoryPerPage();

        historyService.findPlayerUuid(targetName).thenCompose(uuidOpt -> {
            if (uuidOpt.isEmpty()) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        langSupplier.get().send(context.getSource().getSender(),
                                "history.empty", Placeholder.unparsed("player", targetName)));
                return CompletableFuture.completedFuture(null);
            }

            final UUID targetUuid = uuidOpt.get();
            return historyService.countHistory(targetUuid, since).thenCompose(totalCount -> {
                final int maxPage = Math.max(1, (int) Math.ceil((double) totalCount / recordsPerPage));
                final int safePage = Math.min(page, maxPage);
                final int offset = (safePage - 1) * recordsPerPage;

                return historyService.getHistory(targetUuid, since, recordsPerPage, offset)
                        .thenAccept(records ->
                                plugin.getServer().getScheduler().runTask(plugin, () ->
                                        renderHistory(context, records, targetName, timeArg, safePage, maxPage))
                        );
            });
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Failed to retrieve sell history: " + ex.getMessage());
            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private void renderHistory(final CommandContext<CommandSourceStack> context,
                               final List<SellHistoryService.SellRecord> records,
                               final String targetName,
                               final String timeArg,
                               final int page,
                               final int maxPage) {
        final LangConfig lang = langSupplier.get();

        if (records.isEmpty()) {
            lang.send(context.getSource().getSender(), "history.empty",
                    Placeholder.unparsed("player", targetName));
            return;
        }

        final SellService sellService = sellServiceSupplier.get();
        final List<Component> entryComponents = new ArrayList<>();

        final boolean isConsoleSender = !(context.getSource().getSender() instanceof Player);
        final OfflinePlayer viewer = isConsoleSender ? null : (Player) context.getSource().getSender();

        for (final SellHistoryService.SellRecord record : records) {
            final String headTag;
            if (isConsoleSender) {
                headTag = record.playerName();
            } else if (isMojangUuid(record.playerUuid())) {
                headTag = "<head:" + record.playerUuid() + ">";
            } else {
                headTag = "<head:" + DEFAULT_HEAD_UUID + ">";
            }

            final Component entryComponent = lang.get(viewer, "history.entry",
                    Placeholder.parsed("player-head", headTag),
                    Placeholder.parsed("recipe", record.displayName()),
                    Placeholder.unparsed("quantity", String.valueOf(record.quantity())),
                    Placeholder.unparsed("quality", String.format("%.0f%%", record.quality() * 100)),
                    Placeholder.unparsed("total", sellService.format(record.total()))
            );

            final Component hoverComponent = lang.get(viewer, "history.entry-hover",
                    Placeholder.unparsed("player", record.playerName()),
                    Placeholder.unparsed("time-ago", TimeUtil.relativeTime(record.soldAt())),
                    Placeholder.unparsed("exact-date", TimeUtil.exactTime(record.soldAt()))
            );

            entryComponents.add(entryComponent
                    .hoverEvent(HoverEvent.showText(hoverComponent)));
        }

        final Component entriesBlock = Component.join(JoinConfiguration.newlines(), entryComponents);
        final Component previousPage = buildPreviousPage(lang, viewer, targetName, timeArg, page);
        final Component nextPage = buildNextPage(lang, viewer, targetName, timeArg, page, maxPage);

        lang.send(context.getSource().getSender(), "history.display",
                Placeholder.component("entries", entriesBlock),
                Placeholder.component("previous-page", previousPage),
                Placeholder.component("next-page", nextPage),
                Placeholder.unparsed("player", targetName),
                Placeholder.unparsed("page", String.format("%02d", page)),
                Placeholder.unparsed("max-page", String.format("%02d", maxPage)));
    }

    private Component buildPreviousPage(final LangConfig lang,
                                        final OfflinePlayer viewer,
                                        final String targetName,
                                        final String timeArg,
                                        final int currentPage) {
        return buildPageButton(lang, viewer, targetName, timeArg,
                currentPage > 1, currentPage - 1,
                "history.navigation.previous", "history.navigation.previous-hover",
                "history.navigation.previous-disabled");
    }

    private Component buildNextPage(final LangConfig lang,
                                    final OfflinePlayer viewer,
                                    final String targetName,
                                    final String timeArg,
                                    final int currentPage,
                                    final int maxPage) {
        return buildPageButton(lang, viewer, targetName, timeArg,
                currentPage < maxPage, currentPage + 1,
                "history.navigation.next", "history.navigation.next-hover",
                "history.navigation.next-disabled");
    }

    private Component buildPageButton(final LangConfig lang,
                                      final OfflinePlayer viewer,
                                      final String targetName,
                                      final String timeArg,
                                      final boolean enabled,
                                      final int targetPage,
                                      final String textKey,
                                      final String hoverKey,
                                      final String disabledKey) {
        if (!enabled) {
            return lang.get(viewer, disabledKey);
        }
        final Component text = lang.get(viewer, textKey);
        final Component hover = lang.get(viewer, hoverKey, Placeholder.unparsed("page", String.valueOf(targetPage)));
        return text
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand("/tbm history " + targetName + " " + timeArg + " " + targetPage));
    }

    /**
     * Returns true for standard Mojang (online-mode) UUIDs.
     * Bedrock players via Floodgate use version-3 UUIDs; offline-mode players use version-3 as well.
     * This check is used only for player head rendering, not for data lookup.
     */
    private boolean isMojangUuid(final UUID uuid) {
        return uuid.version() == 4;
    }
}