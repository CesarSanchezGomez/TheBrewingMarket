package com.cesarcosmico.brewmarket;

import com.cesarcosmico.brewmarket.command.CommandManager;
import com.cesarcosmico.brewmarket.config.LangConfig;
import com.cesarcosmico.brewmarket.config.MarketConfig;
import com.cesarcosmico.brewmarket.listener.MarketGUIListener;
import com.cesarcosmico.brewmarket.service.BrewPriceService;
import com.cesarcosmico.brewmarket.service.EconomyService;
import com.cesarcosmico.brewmarket.service.SellService;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BrewMarketPlugin extends JavaPlugin {

    private EconomyService economyService;
    private BrewPriceService brewPriceService;
    private SellService sellService;
    private MarketConfig marketConfig;
    private LangConfig langConfig;

    @Override
    public void onEnable() {
        Economy economy = setupEconomy();
        if (economy == null) {
            getLogger().severe("Vault economy not found. Disabling BrewMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("TheBrewingProject") == null) {
            getLogger().severe("TheBrewingProject not found. Disabling BrewMarket.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        this.langConfig = new LangConfig(this);
        this.economyService = new EconomyService(economy);
        this.marketConfig = new MarketConfig(getConfig(), getLogger());
        this.brewPriceService = new BrewPriceService(marketConfig, this::resolveBreweryApi);
        this.sellService = new SellService(brewPriceService, economyService);

        registerCommands();
        getServer().getPluginManager().registerEvents(
                new MarketGUIListener(this, langConfig), this);

        getLogger().info("BrewMarket enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("BrewMarket disabled.");
    }

    public void reload() {
        reloadConfig();
        this.langConfig.load();
        this.marketConfig = new MarketConfig(getConfig(), getLogger());
        this.brewPriceService = new BrewPriceService(marketConfig, this::resolveBreweryApi);
        this.sellService = new SellService(brewPriceService, economyService);
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(
                this::getMarketConfig,
                this::getSellService,
                this::getLangConfig,
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
}