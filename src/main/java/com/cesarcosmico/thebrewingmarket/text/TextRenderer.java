package com.cesarcosmico.thebrewingmarket.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;

/**
 * Single rendering path for viewer-facing text: resolves placeholders, converts any legacy
 * codes to MiniMessage, then deserializes. The {@code viewer} may be null for server-scoped text.
 */
public final class TextRenderer {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final PlaceholderResolver placeholderResolver;

    public TextRenderer(PlaceholderResolver placeholderResolver) {
        this.placeholderResolver = placeholderResolver;
    }

    public Component render(OfflinePlayer viewer, String raw) {
        return MINI.deserialize(prepare(viewer, raw));
    }

    public Component render(OfflinePlayer viewer, String raw, TagResolver tagResolver) {
        return MINI.deserialize(prepare(viewer, raw), tagResolver);
    }

    private String prepare(OfflinePlayer viewer, String raw) {
        return LegacyToMiniMessageConverter.toMiniMessage(placeholderResolver.resolve(viewer, raw));
    }
}