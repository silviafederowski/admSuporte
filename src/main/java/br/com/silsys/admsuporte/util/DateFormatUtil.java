package br.com.silsys.admsuporte.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formata datas para exibicao em tela no padrao dd/mm/aa (JSTL fmt:formatDate nao aceita java.time.LocalDate). */
public final class DateFormatUtil {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    private DateFormatUtil() {
    }

    public static String formatar(LocalDate data) {
        return data != null ? data.format(DATA_FORMATTER) : "";
    }
}
