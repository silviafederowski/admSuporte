package br.com.silsys.admsuporte.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Formata datas/horas para exibicao em tela no padrao dd/mm/aa e HH:mm (JSTL fmt:formatDate nao aceita java.time). */
public final class DateFormatUtil {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DateFormatUtil() {
    }

    public static String formatar(LocalDate data) {
        return data != null ? data.format(DATA_FORMATTER) : "";
    }

    public static String formatarHora(LocalTime hora) {
        return hora != null ? hora.format(HORA_FORMATTER) : "";
    }
}
