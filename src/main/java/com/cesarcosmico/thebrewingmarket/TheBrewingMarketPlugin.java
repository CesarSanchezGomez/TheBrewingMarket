package com.cesarcosmico.thebrewingmarket;

import com.cesarcosmico.thebrewingmarket.command.CommandManager;
import com.cesarcosmico.thebrewingmarket.config.DatabaseConfig;
import com.cesarcosmico.thebrewingmarket.config.LangConfig;
import com.cesarcosmico.thebrewingmarket.config.MarketConfig;
import com.cesarcosmico.thebrewingmarket.listener.MarketGUIListener;
import com.cesarcosmico.thebrewingmarket.service.BrewingPriceService;
import com.cesarcosmico.thebrewingmarket.service.EconomyService;
import com.cesarcosmico.thebrewingmarket.service.SellService;
import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;
import com.cesarcosmico.thebrewingmarket.storage.StorageFactory;
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
import java.util.logging.Level;

public final class TheBrewingMarketPlugin extends JavaPlugin {

    private EconomyService economyService;
    private BrewingPriceService brewPriceService;
    private SellService sellService;
    private MarketConfig marketConfig;
    private LangConfig langConfig;
    private SellHistoryService historyService;

    @Override
    public void onEnable() {
        Economy economy = setupEconomy();
        if (economy == null) {
            getLogger().severe("Vault economy not found. Disabling TheBrewingMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("TheBrewingProject") == null) {
            getLogger().severe("TheBrewingProject not found. Disabling TheBrewingMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        this.langConfig = new LangConfig(this);
        this.economyService = new EconomyService(economy);
        this.marketConfig = new MarketConfig(getConfig(), getLogger());
        this.brewPriceService = new BrewingPriceService(marketConfig, this::resolveBreweryApi);
        this.sellService = new SellService(brewPriceService, economyService);

        File dbFile = new File(getDataFolder(), "database.yml");
        if (!dbFile.exists()) {
            saveResource("database.yml", false);
        }
        FileConfiguration dbYml = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "database.yml"));
        DatabaseConfig dbConfig = new DatabaseConfig(dbYml);
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

        registerCommands();
        getServer().getPluginManager().registerEvents(
                new MarketGUIListener(this, langConfig, historyService), this);

        getLogger().info("TheBrewingMarket enabled.");
    }

    @Override
    public void onDisable() {
        if (historyService != null) {
            historyService.shutdown();
        }
        getLogger().info("TheBrewingMarket disabled.");
    }

    public void reload() {
        reloadConfig();
        this.langConfig.load();
        this.marketConfig = new MarketConfig(getConfig(), getLogger());
        this.brewPriceService = new BrewingPriceService(marketConfig, this::resolveBreweryApi);
        this.sellService = new SellService(brewPriceService, economyService);
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(
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
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    private TheBrewingProjectApi resolveBreweryApi() {
        RegisteredServiceProvider<TheBrewingProjectApi> provider =
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