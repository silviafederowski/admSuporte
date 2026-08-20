package br.com.silsys.admsuporte.util;

import java.security.SecureRandom;

/** Gera o codigo numerico de 6 digitos usado na recuperacao de senha (simulada). */
public final class ResetCodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ResetCodeUtil() {
    }

    public static String generate() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
