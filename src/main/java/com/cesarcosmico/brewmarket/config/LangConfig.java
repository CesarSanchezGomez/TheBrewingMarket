package com.cesarcosmico.brewmarket.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

        String lang = plugin.getConfig().getString("lang", "en");
        File langFolder = new File(plugin.getDataFolder(), "lang");

        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveDefaultLang("en");
        saveDefaultLang("es");

        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found. Falling back to 'en'.");
            langFile = new File(langFolder, "en.yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = plugin.getResource("lang/en.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }

        this.prefix = config.getString("prefix", "<gray>[<gold>BrewMarket</gold>]</gray>");

        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                messages.put(key, config.getString(key, key));
            }
        }

        plugin.getLogger().info("Loaded language: " + lang);
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

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    private void saveDefaultLang(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }
    }
}