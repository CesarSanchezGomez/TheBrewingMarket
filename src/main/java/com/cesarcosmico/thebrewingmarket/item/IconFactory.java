package com.cesarcosmico.thebrewingmarket.item;

import com.cesarcosmico.thebrewingmarket.config.IconConfig;
import com.cesarcosmico.thebrewingmarket.item.component.ComponentRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class IconFactory {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final Logger logger;
    private final ComponentRegistry componentRegistry;

    public IconFactory(Logger logger) {
        this.logger = logger;
        this.componentRegistry = new ComponentRegistry(logger);
    }

    public ItemStack buildStaticIcon(ConfigurationSection section) {
        Material material = resolveMaterial(section);
        ItemStack item = new ItemStack(material);

        componentRegistry.applyAll(item, section);

        return item;
    }

    public IconConfig parseIconConfig(ConfigurationSection section) {
        if (section == null) {
            return new IconConfig(new ItemStack(Material.BARRIER), null, List.of(), " ");
        }

        Material material = resolveMaterial(section);
        ItemStack baseItem = new ItemStack(material);

        componentRegistry.applyAll(baseItem, section);

        String displayNameRaw = section.getString("custom_name", " ");
        List<String> loreRaw = section.getStringList("lore");

        String sound = null;

        ConfigurationSection actionSection = section.getConfigurationSection("action");
        if (actionSection != null) {
            for (String actionKey : actionSection.getKeys(false)) {
                ConfigurationSection action = actionSection.getConfigurationSection(actionKey);
                if (action == null) continue;
                String type = action.getString("type", "");
                if ("sound".equals(type)) {
                    sound = action.getString("value", "");
                }
            }
        }

        return new IconConfig(baseItem, sound, loreRaw, displayNameRaw);
    }

    public ItemStack buildDynamicIcon(IconConfig config, String money, String soldAmount) {
        ItemStack item = config.baseItem().clone();
        ItemMeta meta = item.getItemMeta();

        String nameRaw = config.displayNameRaw()
                .replace("{money}", money)
                .replace("{sold_amount}", soldAmount);
        meta.displayName(mm(nameRaw));

        if (!config.loreRaw().isEmpty()) {
            List<Component> lore = config.loreRaw().stream()
                    .map(line -> line.replace("{money}", money).replace("{sold_amount}", soldAmount))
                    .map(IconFactory::mm)
                    .toList();
            meta.lore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public Map<Character, ItemStack> parseDecorativeIcons(ConfigurationSection market) {
        Map<Character, ItemStack> result = new HashMap<>();

        ConfigurationSection decoSection = market.getConfigurationSection("decorative-icons");
        if (decoSection != null) {
            for (String key : decoSection.getKeys(false)) {
                ConfigurationSection icon = decoSection.getConfigurationSection(key);
                if (icon == null) continue;
                char symbol = icon.getString("symbol", "?").charAt(0);
                result.put(symbol, buildStaticIcon(icon));
            }
        }

        parseNamedIcon(market, "title-icon", result);
        parseNamedIcon(market, "close-icon", result);

        return result;
    }

    private void parseNamedIcon(ConfigurationSection market, String key, Map<Character, ItemStack> target) {
        ConfigurationSection section = market.getConfigurationSection(key);
        if (section == null) return;
        char symbol = section.getString("symbol", "?").charAt(0);
        target.put(symbol, buildStaticIcon(section));
    }

    private Material resolveMaterial(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null) {
            logger.warning("Unknown material: " + section.getString("material") + ", using STONE");
            material = Material.STONE;
        }
        return material;
    }

    private static Component mm(String raw) {
        return MINI.deserialize(raw).decoration(TextDecoration.ITALIC, false);
    }
}