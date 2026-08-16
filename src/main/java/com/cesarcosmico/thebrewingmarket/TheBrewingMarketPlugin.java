package com.cesarcosmico.thebrewingmarket;

import com.cesarcosmico.thebrewingmarket.command.CommandManager;
import com.cesarcosmico.thebrewingmarket.config.ConfigVersionChecker;
import com.cesarcosmico.thebrewingmarket.config.DatabaseConfig;
import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.listener.MarketGUIListener;
import com.cesarcosmico.thebrewingmarket.brew.BrewResolver;
import com.cesarcosmico.thebrewingmarket.brew.BreweryXBrewResolver;
import com.cesarcosmico.thebrewingmarket.brew.TBPBrewResolver;
import com.cesarcosmico.thebrewingmarket.integration.placeholderapi.PapiPlaceholderResolver;
import com.cesarcosmico.thebrewingmarket.integration.placeholderapi.TBMExpansion;
import com.cesarcosmico.thebrewingmarket.service.BrewEvaluator;
import com.cesarcosmico.thebrewingmarket.service.DailyEarningsTracker;
import com.cesarcosmico.thebrewingmarket.service.EconomyService;
import com.cesarcosmico.thebrewingmarket.service.MarketAnalyticsCache;
import com.cesarcosmico.thebrewingmarket.service.MoneyFormatter;
import com.cesarcosmico.thebrewingmarket.service.PlayerStatsCache;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.storage.StorageFactory;
import com.cesarcosmico.thebrewingmarket.text.PlaceholderResolver;
import com.cesarcosmico.thebrewingmarket.text.TextRenderer;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

public final class TheBrewingMarketPlugin extends JavaPlugin {

    private static final Set<String> CONFIG_REQUIRED_KEYS = Set.of(
            "lang",
            "history-per-page",
            "market.shulker-selling",
            "market.money-format",
            "market.title",
            "market.limitation",
            "market.item-slot");

    private EconomyService economyService;
    private BrewEvaluator brewEvaluator;
    private SellService sellService;
    private MarketConfig marketConfig;
    private LangConfig langConfig;
    private TextRenderer textRenderer;
    private SellHistoryService historyService;
    private BrewResolver brewResolver;
    private DailyEarningsTracker dailyEarningsTracker;
    private PlayerStatsCache playerStatsCache;
    private MarketAnalyticsCache marketAnalyticsCache;

    @Override
    public void onEnable() {
        final Economy economy = setupEconomy();
        if (economy == null) {
            getLogger().severe("Vault economy not found. Disabling TheBrewingMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final boolean hasTBP      = getServer().getPluginManager().getPlugin("TheBrewingProject") != null;
        final boolean hasBreweryX = getServer().getPluginManager().getPlugin("BreweryX") != null;

        if (!hasTBP && !hasBreweryX) {
            getLogger().severe("Missing TBP/BreweryX. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (hasTBP) {
            getLogger().info("Hooked into TheBrewingProject.");
            this.brewResolver = new TBPBrewResolver(this::resolveBreweryApi);
        } else {
            getLogger().info("Hooked into BreweryX.");
            this.brewResolver = new BreweryXBrewResolver();
        }

        saveDefaultConfig();
        ConfigVersionChecker.check(getConfig(), "config.yml",
                MarketConfig.CURRENT_VERSION, this, getLogger(), CONFIG_REQUIRED_KEYS);

        final boolean hasPlaceholderApi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        final PlaceholderResolver placeholderResolver =
                hasPlaceholderApi ? new PapiPlaceholderResolver() : PlaceholderResolver.NONE;
        this.textRenderer = new TextRenderer(placeholderResolver);

        this.langConfig = new LangConfig(this, textRenderer);
        this.economyService = new EconomyService(economy);
        this.marketConfig = new MarketConfig(getConfig(), getLogger(), textRenderer);
        this.brewEvaluator = new BrewEvaluator(marketConfig, brewResolver);
        this.sellService = new SellService(brewEvaluator, economyService,
                new MoneyFormatter(marketConfig.getMoneyFormatPattern()));

        final File dbFile = new File(getDataFolder(), "database.yml");
        if (!dbFile.exists()) {
            saveResource("database.yml", false);
        }
        final FileConfiguration dbYml = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "database.yml"));
        ConfigVersionChecker.check(dbYml, "database.yml",
                DatabaseConfig.CURRENT_VERSION, this, getLogger());
        final DatabaseConfig dbConfig = new DatabaseConfig(dbYml);
        try {
            this.historyService = StorageFactory.create(dbConfig, getDataFolder().toPath(), getLogger());
            this.historyService.initialize();
        } catch (IllegalArgumentException e) {
            getLogger().severe("Invalid data-storage-method in database.yml: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.dailyEarningsTracker = new DailyEarningsTracker(historyService);
        this.playerStatsCache = new PlayerStatsCache(historyService);
        this.marketAnalyticsCache = new MarketAnalyticsCache(historyService);
        this.marketAnalyticsCache.start(this);

        registerCommands();
        getServer().getPluginManager().registerEvents(
                new MarketGUIListener(this, langConfig, historyService,
                        dailyEarningsTracker, playerStatsCache, this::getMarketConfig), this);

        if (hasPlaceholderApi) {
            for (String id : new String[] {"tbm", "thebrewingmarket"}) {
                new TBMExpansion(id, this, this::getMarketConfig, langConfig,
                        dailyEarningsTracker, playerStatsCache,
                        marketAnalyticsCache, sellService).register();
            }
            getLogger().info("PlaceholderAPI expansions registered: tbm, thebrewingmarket.");
        }

        getLogger().info("TheBrewingMarket enabled.");
    }

    @Override
    public void onDisable() {
        if (marketAnalyticsCache != null) {
            marketAnalyticsCache.stop();
        }
        if (historyService != null) {
            historyService.shutdown();
        }
        getLogger().info("TheBrewingMarket disabled.");
    }

    public void reload() {
        reloadConfig();
        ConfigVersionChecker.check(getConfig(), "config.yml",
                MarketConfig.CURRENT_VERSION, this, getLogger(), CONFIG_REQUIRED_KEYS);
        this.langConfig.load();
        this.marketConfig = new MarketConfig(getConfig(), getLogger(), textRenderer);
        this.brewEvaluator = new BrewEvaluator(marketConfig, brewResolver);
        this.sellService = new SellService(brewEvaluator, economyService,
                new MoneyFormatter(marketConfig.getMoneyFormatPattern()));
    }

    private void registerCommands() {
        final CommandManager commandManager = new CommandManager(
                this::getMarketConfig,
                this::getSellService,
                this::getLangConfig,
                this::getHistoryService,
                this,
                this::reload
        );

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(commandManager.createCommand());
            event.registrar().register(commandManager.createAliasCommand());
        });
    }

    private Economy setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        final RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    private TheBrewingProjectApi resolveBreweryApi() {
        final RegisteredServiceProvider<TheBrewingProjectApi> provider =
                Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi.class);
        return provider != null ? provider.getProvider() : null;
    }

    public SellService getSellService() {
        return sellService;
    }

    public MarketConfig getMarketConfig() {
        return marketConfig;
    }

    public LangConfig getLangConfig() {
        return langConfig;
    }

    public SellHistoryService getHistoryService() {
        return historyService;
    }
}