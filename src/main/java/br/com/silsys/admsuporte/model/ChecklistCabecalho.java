package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Dados do checklist como um todo (tabela checklist_cabecalho, um por data+usuario). */
public class ChecklistCabecalho implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter VERIFICADO_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    /** Valores de status (coluna checklist_cabecalho.status). */
    public static final String EM_ANDAMENTO = "em_andamento";
    public static final String DISPONIVEL = "disponivel";
    public static final String VALIDADA = "validada";

    private int encomendasSemBaixa;
    private String status = EM_ANDAMENTO;
    private String verificadoPor;
    private LocalDateTime verificadoEm;
    private String verificacaoObs;

    public int getEncomendasSemBaixa() {
        return encomendasSemBaixa;
    }

    public void setEncomendasSemBaixa(int encomendasSemBaixa) {
        this.encomendasSemBaixa = encomendasSemBaixa;
    }

    public String getVerificadoPor() {
        return verificadoPor;
    }

    public void setVerificadoPor(String verificadoPor) {
        this.verificadoPor = verificadoPor;
    }

    public LocalDateTime getVerificadoEm() {
        return verificadoEm;
    }

    public void setVerificadoEm(LocalDateTime verificadoEm) {
        this.verificadoEm = verificadoEm;
    }

    public String getVerificadoEmFormatado() {
        return verificadoEm != null ? verificadoEm.format(VERIFICADO_FORMATTER) : "";
    }

    public String getVerificacaoObs() {
        return verificacaoObs;
    }

    public void setVerificacaoObs(String verificacaoObs) {
        this.verificacaoObs = verificacaoObs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel(status);
    }

    /** Validada: depois disso ninguem altera mais nada no checklist. */
    public boolean isVerificado() {
        return VALIDADA.equals(status);
    }

    public static String statusLabel(String status) {
        if (DISPONIVEL.equals(status)) {
            return "Disponível para validação";
        }
        if (VALIDADA.equals(status)) {
            return "Validada";
        }
        return "Em andamento";
    }
}
