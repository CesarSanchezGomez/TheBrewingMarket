package com.cesarcosmico.thebrewingmarket.brew;

import com.dre.brewery.Brew;
import com.dre.brewery.lore.Base91DecoderStream;
import com.dre.brewery.lore.LoreLoadStream;
import com.dre.brewery.lore.NBTLoadStream;
import com.dre.brewery.lore.XORUnscrambleStream;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads BreweryX's scrambled NBT/lore payload directly — fallback for when
 * {@link Brew#get(ItemStack)} returns null (e.g. recipe renamed or removed).
 */
final class BreweryXRawDecoder {

    private static final Logger LOGGER = Logger.getLogger(BreweryXRawDecoder.class.getName());

    static final int MAX_QUALITY = 10;
    private static final byte SUPPORTED_VERSION = 1;

    // Bitmask flags from com.dre.brewery.Brew.saveToStream — must stay in sync.
    private static final int BIT_DISTILL_RUNS = 1;
    private static final int BIT_AGE_TIME     = 2;
    private static final int BIT_WOOD         = 4;
    private static final int BIT_RECIPE_NAME  = 8;
    private static final int BIT_ALC          = 64;

    private BreweryXRawDecoder() {
    }

    static Optional<RawBrewData> decode(final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        final InputStream payload = openPayloadStream(meta);
        if (payload == null) {
            return Optional.empty();
        }

        final XORUnscrambleStream unscrambler =
                new XORUnscrambleStream(payload, readSaveSeed(), Brew.getPrevSeeds());

        try (DataInputStream in = new DataInputStream(unscrambler)) {
            // Parity and version are written unscrambled before start().
            in.readByte();
            final byte version = in.readByte();
            if (version != SUPPORTED_VERSION) {
                return Optional.empty();
            }

            unscrambler.start();

            final int quality = in.readByte();
            final int bools   = in.readUnsignedByte();

            if ((bools & BIT_ALC) != 0)          in.readShort();
            if ((bools & BIT_DISTILL_RUNS) != 0) in.readByte();
            if ((bools & BIT_AGE_TIME) != 0)     in.readFloat();
            if ((bools & BIT_WOOD) != 0)         in.readFloat();

            final String recipeName = (bools & BIT_RECIPE_NAME) != 0 ? in.readUTF() : null;

            return Optional.of(new RawBrewData(recipeName, clampQuality(quality)));
        } catch (final Exception ex) {
            LOGGER.log(Level.WARNING, "BreweryX raw decode failed: " + ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private static InputStream openPayloadStream(final ItemMeta meta) {
        try {
            final NBTLoadStream nbt = new NBTLoadStream(meta);
            if (nbt.hasData()) {
                return nbt;
            }
        } catch (final Throwable ignored) {
        }
        try {
            return new Base91DecoderStream(new LoreLoadStream(meta, 0));
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Uses reflection on the private saveSeed field; falls back to the last
     * prev seed since BreweryX's updatePrevSeeds() always includes the current one.
     */
    private static long readSaveSeed() {
        try {
            final Field field = Brew.class.getDeclaredField("saveSeed");
            field.setAccessible(true);
            return field.getLong(null);
        } catch (final ReflectiveOperationException | RuntimeException ignored) {
            final List<Long> prev = Brew.getPrevSeeds();
            return prev.isEmpty() ? 0L : prev.get(prev.size() - 1);
        }
    }

    private static int clampQuality(final int quality) {
        if (quality < 0) return 0;
        if (quality > MAX_QUALITY) return MAX_QUALITY;
        return quality;
    }

    record RawBrewData(String recipeName, int quality) {
    }
}