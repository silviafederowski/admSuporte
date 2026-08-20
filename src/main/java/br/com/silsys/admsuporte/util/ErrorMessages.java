package br.com.silsys.admsuporte.util;

/** Formata uma descricao curta de uma excecao para mostrar na tela (alem de logar o stack trace inteiro). */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String describe(Throwable t) {
        String message = t.getMessage();
        String detail = t.getClass().getSimpleName() + (message != null && !message.isEmpty() ? ": " + message : "");
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            detail += " (causa: " + describe(cause) + ")";
        }
        return detail;
    }
}
