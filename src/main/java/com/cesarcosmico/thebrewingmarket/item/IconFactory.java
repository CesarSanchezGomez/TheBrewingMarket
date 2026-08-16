package com.cesarcosmico.thebrewingmarket.item;

import com.cesarcosmico.thebrewingmarket.config.IconConfig;
import com.cesarcosmico.thebrewingmarket.item.component.ComponentRegistry;
import com.cesarcosmico.thebrewingmarket.text.TextRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class IconFactory {

    private final Logger logger;
    private final TextRenderer textRenderer;
    private final ComponentRegistry componentRegistry;

    public IconFactory(Logger logger, TextRenderer textRenderer) {
        this.logger = logger;
        this.textRenderer = textRenderer;
        this.componentRegistry = new ComponentRegistry(logger);
    }

    public IconConfig parseIconConfig(ConfigurationSection section) {
        if (section == null) {
            return new IconConfig(new ItemStack(Material.BARRIER), null, List.of(), null);
        }

        ItemStack baseItem = buildBaseItem(section);
        ConfigurationSection components = section.getConfigurationSection("components");
        String displayNameRaw = components != null ? components.getString("custom-name") : null;
        List<String> loreRaw = components != null ? components.getStringList("lore") : List.of();
        String sound = extractSound(section);

        return new IconConfig(baseItem, sound, loreRaw, displayNameRaw);
    }

    public ItemStack render(IconConfig config, OfflinePlayer viewer, TagResolver resolver) {
        ItemStack item = config.baseItem().clone();
        ItemMeta meta = item.getItemMeta();

        if (config.displayNameRaw() != null) {
            meta.displayName(renderLine(viewer, config.displayNameRaw(), resolver));
        }
        if (!config.loreRaw().isEmpty()) {
            meta.lore(config.loreRaw().stream()
                    .map(line -> renderLine(viewer, line, resolver))
                    .toList());
        }

        item.setItemMeta(meta);
        return item;
    }

    public Map<Character, IconConfig> parseDecorativeIcons(ConfigurationSection market) {
        Map<Character, IconConfig> result = new HashMap<>();

        ConfigurationSection decoSection = market.getConfigurationSection("decorative-icons");
        if (decoSection != null) {
            for (String key : decoSection.getKeys(false)) {
                ConfigurationSection icon = decoSection.getConfigurationSection(key);
                if (icon == null) continue;
                char symbol = icon.getString("symbol", "?").charAt(0);
                result.put(symbol, parseIconConfig(icon));
            }
        }

        parseNamedIcon(market, "title-icon", result);
        parseNamedIcon(market, "close-icon", result);

        return result;
    }

    private ItemStack buildBaseItem(ConfigurationSection section) {
        Material material = resolveMaterial(section);
        ItemStack item = new ItemStack(material);
        ConfigurationSection components = section.getConfigurationSection("components");
        if (components != null) {
            componentRegistry.applyAll(item, components);
        }
        return item;
    }

    private Component renderLine(OfflinePlayer viewer, String raw, TagResolver resolver) {
        return textRenderer.render(viewer, raw, resolver).decoration(TextDecoration.ITALIC, false);
    }

    private String extractSound(ConfigurationSection section) {
        ConfigurationSection actionSection = section.getConfigurationSection("action");
        if (actionSection == null) return null;

        for (String actionKey : actionSection.getKeys(false)) {
            ConfigurationSection action = actionSection.getConfigurationSection(actionKey);
            if (action == null) continue;
            if ("sound".equals(action.getString("type", ""))) {
                return action.getString("value", "");
            }
        }
        return null;
    }

    private void parseNamedIcon(ConfigurationSection market, String key, Map<Character, IconConfig> target) {
        ConfigurationSection section = market.getConfigurationSection(key);
        if (section == null) return;
        char symbol = section.getString("symbol", "?").charAt(0);
        target.put(symbol, parseIconConfig(section));
    }

    private Material resolveMaterial(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null) {
            logger.warning("Unknown material: " + section.getString("material") + ", using STONE");
            material = Material.STONE;
        }
        return material;
    }
}