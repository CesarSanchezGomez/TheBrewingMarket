package com.cesarcosmico.thebrewingmarket.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class MiniMessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private MiniMessageUtil() {}

    public static String toPlainText(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) return "";
        return PLAIN.serialize(MINI.deserialize(miniMessage));
    }

    public static String toPlainText(Component component) {
        if (component == null) return "";
        return PLAIN.serialize(component);
    }
}
