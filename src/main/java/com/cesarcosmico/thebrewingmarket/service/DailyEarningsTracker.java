package com.cesarcosmico.thebrewingmarket.service;

import com.cesarcosmico.thebrewingmarket.storage.SellHistoryService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DailyEarningsTracker {

    private record Entry(LocalDate day, double amount) {}

    private final SellHistoryService historyService;
    private final ConcurrentHashMap<UUID, Entry> cache = new ConcurrentHashMap<>();

    public DailyEarningsTracker(SellHistoryService historyService) {
        this.historyService = historyService;
    }

    public void seedAsync(UUID uuid) {
        long startOfDay = startOfLocalDayMillis();
        historyService.sumTotalSince(uuid, startOfDay).thenAccept(sum ->
                cache.put(uuid, new Entry(LocalDate.now(), sum)));
    }

    public double getTodayEarnings(UUID uuid) {
        Entry entry = cache.get(uuid);
        if (entry == null) return 0.0;
        if (!entry.day().equals(LocalDate.now())) {
            cache.put(uuid, new Entry(LocalDate.now(), 0.0));
            return 0.0;
        }
        return entry.amount();
    }

    public void record(UUID uuid, double amount) {
        LocalDate today = LocalDate.now();
        cache.merge(uuid, new Entry(today, amount), (old, inc) ->
                old.day().equals(today)
                        ? new Entry(today, old.amount() + inc.amount())
                        : new Entry(today, inc.amount()));
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    private static long startOfLocalDayMillis() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
