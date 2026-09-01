package br.com.silsys.admsuporte.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Mesmas regras de validacao do app mobile (src/utils/validators.ts). */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Locale LOCALE_PT_BR = new Locale("pt", "BR");

    private ValidationUtil() {
    }

    public static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidPhone(String value) {
        if (value == null) {
            return false;
        }
        String digits = normalizePhone(value);
        return digits.length() >= 8 && digits.length() <= 15;
    }

    public static String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Aparas e converte para maiusculas (locale pt-BR, preserva acentos); null/vazio vira null. */
    public static String toUpperOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(LOCALE_PT_BR);
    }
}
