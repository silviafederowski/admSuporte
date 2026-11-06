package br.com.silsys.admsuporte.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Formata datas/horas para exibicao em tela no padrao dd/mm/aa e HH:mm (JSTL fmt:formatDate nao aceita java.time). */
public final class DateFormatUtil {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /** O servidor da hospedagem roda em UTC; sem isso, depois das 21h "hoje" ja seria o dia seguinte. */
    private static final ZoneId FUSO_CONDOMINIO = ZoneId.of("America/Sao_Paulo");

    private DateFormatUtil() {
    }

    /** Data de hoje no fuso do condominio, independente do fuso da JVM. */
    public static LocalDate hoje() {
        return LocalDate.now(FUSO_CONDOMINIO);
    }

    /** Data e hora de agora no fuso do condominio (ex.: carimbo no nome de arquivos gerados). */
    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO_CONDOMINIO);
    }

    public static String formatar(LocalDate data) {
        return data != null ? data.format(DATA_FORMATTER) : "";
    }

    public static String formatarHora(LocalTime hora) {
        return hora != null ? hora.format(HORA_FORMATTER) : "";
    }
}
