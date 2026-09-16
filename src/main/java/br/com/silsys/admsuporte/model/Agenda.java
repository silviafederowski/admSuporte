package br.com.silsys.admsuporte.model;

import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Agenda implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private LocalDate data;
    private LocalTime hora;
    private String pontoFocal;
    private String assunto;
    private String observacao;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    /** Formatada dd/mm/aa, para exibicao em tela. */
    public String getDataFormatada() {
        return DateFormatUtil.formatar(data);
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    /** Formatada HH:mm, para exibicao em tela. */
    public String getHoraFormatada() {
        return DateFormatUtil.formatarHora(hora);
    }

    public String getPontoFocal() {
        return pontoFocal;
    }

    public void setPontoFocal(String pontoFocal) {
        this.pontoFocal = pontoFocal;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isHoje() {
        return data != null && data.isEqual(LocalDate.now());
    }
}
