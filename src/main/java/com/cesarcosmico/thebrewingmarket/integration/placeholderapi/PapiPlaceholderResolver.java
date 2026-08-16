package com.cesarcosmico.thebrewingmarket.integration.placeholderapi;

import com.cesarcosmico.thebrewingmarket.text.PlaceholderResolver;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

/**
 * Bridges to PlaceholderAPI. Instantiated only when PlaceholderAPI is installed so the
 * hard reference here is never linked on servers without it.
 */
public final class PapiPlaceholderResolver implements PlaceholderResolver {

    @Override
    public String resolve(OfflinePlayer viewer, String text) {
        return PlaceholderAPI.setPlaceholders(viewer, text);
    }
}