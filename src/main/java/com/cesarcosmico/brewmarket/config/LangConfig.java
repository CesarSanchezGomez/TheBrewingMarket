package com.cesarcosmico.brewmarket.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LangConfig {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final Map<String, String> messages;
    private String prefix;

    public LangConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        load();
    }

    public void load() {
        messages.clear();

        String lang = plugin.getConfig().getString("lang", "en_US");
        File langFolder = new File(plugin.getDataFolder(), "lang");

        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveDefaultLang("en_US");
        saveDefaultLang("es_ES");

        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found. Falling back to 'en_US'.");
            langFile = new File(langFolder, "en_US.yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = plugin.getResource("lang/en_US.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }

        this.prefix = config.getString("prefix", "<white>[<gradient:#C173FF:#950DFF>BrewMarket</gradient>]</white>");

        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) continue;
            if (config.isList(key)) {
                messages.put(key, String.join("\n", config.getStringList(key)));
            } else {
                messages.put(key, config.getString(key, key));
            }
        }
    }

    public String getRaw(String key) {
        return messages.getOrDefault(key, "<red>Missing message: " + key + "</red>");
    }

    public Component get(String key, String... placeholders) {
        String raw = getRaw(key).replace("{prefix}", prefix);

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace(placeholders[i], placeholders[i + 1]);
        }

        return MINI.deserialize(raw);
    }

    public Component get(String key, TagResolver resolver,
                         String[] componentKeys, String... placeholders) {
        String raw = getRaw(key).replace("{prefix}", prefix);

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace(placeholders[i], placeholders[i + 1]);
        }

        for (String componentKey : componentKeys) {
            raw = raw.replace("{" + componentKey + "}", "<" + componentKey + ">");
        }

        return MINI.deserialize(raw, resolver);
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    public void send(CommandSender sender, String key, TagResolver resolver,
                     String[] componentKeys, String... placeholders) {
        sender.sendMessage(get(key, resolver, componentKeys, placeholders));
    }

    private void saveDefaultLang(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }
    }
}