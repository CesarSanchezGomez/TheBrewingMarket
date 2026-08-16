package com.cesarcosmico.thebrewingmarket.text;

import org.bukkit.OfflinePlayer;

@FunctionalInterface
public interface PlaceholderResolver {

    PlaceholderResolver NONE = (viewer, text) -> text;

    String resolve(OfflinePlayer viewer, String text);
}