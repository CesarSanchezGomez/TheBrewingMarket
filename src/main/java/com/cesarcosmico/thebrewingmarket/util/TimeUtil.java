package com.cesarcosmico.thebrewingmarket.util;

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

    /**
     * Parses a time range string (e.g. "1d", "2w", "3M", "1Y") and returns
     * the epoch millis cutoff representing "now minus the duration".
     *
     * @throws IllegalArgumentException if the format is invalid
     */
    public static long parseTimeRange(String input) {
        if (input == null || input.length() < 2) {
            throw new IllegalArgumentException("Invalid time format: " + input);
        }

        char unit = input.charAt(input.length() - 1);
        String numberPart = input.substring(0, input.length() - 1);

        int amount;
        try {
            amount = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time format: " + input);
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Time amount must be positive: " + input);
        }

        long millis = switch (unit) {
            case 's' -> TimeUnit.SECONDS.toMillis(amount);
            case 'm' -> TimeUnit.MINUTES.toMillis(amount);
            case 'h' -> TimeUnit.HOURS.toMillis(amount);
            case 'd' -> TimeUnit.DAYS.toMillis(amount);
            case 'w' -> TimeUnit.DAYS.toMillis((long) amount * 7);
            case 'M' -> TimeUnit.DAYS.toMillis((long) amount * 30);
            case 'Y' -> TimeUnit.DAYS.toMillis((long) amount * 365);
            default -> throw new IllegalArgumentException("Invalid time format: " + input);
        };

        return System.currentTimeMillis() - millis;
    }
}