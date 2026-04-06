package com.cesarcosmico.thebrewingmarket.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public final class EconomyService {

    private final Economy economy;

    public EconomyService(Economy economy) {
        this.economy = economy;
    }

    public boolean deposit(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public String format(double amount) {
        return economy.format(amount);
    }
}