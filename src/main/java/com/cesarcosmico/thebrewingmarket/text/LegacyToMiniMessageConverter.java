package com.cesarcosmico.thebrewingmarket.text;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts legacy colour/format codes ({@code &} or {@code §}) into MiniMessage tags so text
 * from third-party sources (e.g. PlaceholderAPI output) renders instead of crashing the parser.
 * Tags are emitted without closing counterparts, mirroring legacy forward-application semantics.
 */
public final class LegacyToMiniMessageConverter {

    private static final Pattern SPIGOT_HEX = Pattern.compile("[&§]x((?:[&§][0-9a-fA-F]){6})");
    private static final Pattern SHORT_HEX = Pattern.compile("[&§]#([0-9a-fA-F]{6})");
    private static final Pattern CODE = Pattern.compile("[&§]([0-9a-fk-orA-FK-OR])");
    private static final Pattern SIGN = Pattern.compile("[&§]");

    private static final Map<Character, String> TAGS = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"),
            Map.entry('f', "white"), Map.entry('k', "obfuscated"), Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"), Map.entry('n', "underlined"), Map.entry('o', "italic"),
            Map.entry('r', "reset"));

    private LegacyToMiniMessageConverter() {
    }

    public static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) return input;
        String out = replace(input, SPIGOT_HEX, m -> "<#" + SIGN.matcher(m.group(1)).replaceAll("") + ">");
        out = SHORT_HEX.matcher(out).replaceAll("<#$1>");
        return replace(out, CODE, m -> "<" + TAGS.get(Character.toLowerCase(m.group(1).charAt(0))) + ">");
    }

    private static String replace(String input, Pattern pattern, java.util.function.Function<Matcher, String> replacer) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacer.apply(matcher)));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }
}