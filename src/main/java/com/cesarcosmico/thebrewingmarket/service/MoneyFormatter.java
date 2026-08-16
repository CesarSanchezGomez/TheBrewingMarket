package com.cesarcosmico.thebrewingmarket.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats monetary amounts as a plain number, independent of the economy provider's currency
 * name/colour. Output carries no formatting codes, so it is always MiniMessage-safe.
 */
public final class MoneyFormatter {

    public static final String DEFAULT_PATTERN = "#,##0.##";

    private final String pattern;
    private final DecimalFormatSymbols symbols;

    public MoneyFormatter(String pattern) {
        this.symbols = DecimalFormatSymbols.getInstance(Locale.US);
        this.pattern = isValid(pattern) ? pattern : DEFAULT_PATTERN;
    }

    public String format(double amount) {
        return new DecimalFormat(pattern, symbols).format(amount);
    }

    private boolean isValid(String candidate) {
        if (candidate == null) return false;
        try {
            new DecimalFormat(candidate, symbols);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}