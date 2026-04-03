package com.cesarcosmico.brewmarket.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class TimeUtil {

    private static final DateTimeFormatter EXACT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z").withZone(ZoneId.systemDefault());

    private TimeUtil() {
    }

    public static String relativeTime(long epochMillis) {
        long diff = System.currentTimeMillis() - epochMillis;
        if (diff < 0) return "just now";

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds < 60) {
            return seconds + "s ago";
        }

        double minutes = diff / 60_000.0;
        if (minutes < 60) {
            return String.format("%.2fm ago", minutes);
        }

        double hours = diff / 3_600_000.0;
        if (hours < 24) {
            return String.format("%.2fh ago", hours);
        }

        double days = diff / 86_400_000.0;
        if (days < 7) {
            return String.format("%.1fd ago", days);
        }

        double weeks = diff / 604_800_000.0;
        return String.format("%.1fw ago", weeks);
    }

    public static String exactTime(long epochMillis) {
        return EXACT_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }
}