package com.cesarcosmico.brewmarket.item.component;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TooltipDisplayApplier implements ComponentApplier {

    private static final Map<String, DataComponentType> TYPE_REGISTRY = buildTypeRegistry();

    private final Logger logger;

    public TooltipDisplayApplier(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String key() {
        return "tooltip_display";
    }

    @Override
    public void apply(ItemStack item, ConfigurationSection section) {
        ConfigurationSection tooltipSection = section.getConfigurationSection(key());
        if (tooltipSection == null) return;

        TooltipDisplay.Builder builder = TooltipDisplay.tooltipDisplay();

        if (tooltipSection.getBoolean("hide_tooltip", false)) {
            builder.hideTooltip(true);
        }

        for (String componentId : tooltipSection.getStringList("hidden_components")) {
            String normalized = componentId.replace("minecraft:", "").toLowerCase();
            DataComponentType type = TYPE_REGISTRY.get(normalized);

            if (type == null) {
                logger.warning("Unknown component for tooltip_display hidden_components: " + componentId);
                continue;
            }

            builder.addHiddenComponents(type);
        }

        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, builder.build());
    }

    private static Map<String, DataComponentType> buildTypeRegistry() {
        Map<String, DataComponentType> map = new HashMap<>();
        for (Field field : DataComponentTypes.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isPublic(field.getModifiers())
                    && DataComponentType.class.isAssignableFrom(field.getType())) {
                try {
                    map.put(field.getName().toLowerCase(), (DataComponentType) field.get(null));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return map;
    }
}