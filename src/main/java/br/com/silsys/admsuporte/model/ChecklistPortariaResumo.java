package br.com.silsys.admsuporte.model;

import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/** Uma linha da lista de checklists da portaria: cada (data, usuario) distinto, com a contagem de itens. */
public class ChecklistPortariaResumo implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate data;
    private LocalTime hora;
    private String usuario;
    private int totalItens;
    private int itensChecados;
    private Integer encomendasSemBaixa;
    private String verificadoPor;
    private String status = ChecklistCabecalho.EM_ANDAMENTO;

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDataFormatada() {
        return DateFormatUtil.formatar(data);
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getHoraFormatada() {
        return DateFormatUtil.formatarHora(hora);
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getTotalItens() {
        return totalItens;
    }

    public void setTotalItens(int totalItens) {
        this.totalItens = totalItens;
    }

    public int getItensChecados() {
        return itensChecados;
    }

    public void setItensChecados(int itensChecados) {
        this.itensChecados = itensChecados;
    }

    /** Null em checklist gravado antes de o campo existir. */
    public Integer getEncomendasSemBaixa() {
        return encomendasSemBaixa;
    }

    public void setEncomendasSemBaixa(Integer encomendasSemBaixa) {
        this.encomendasSemBaixa = encomendasSemBaixa;
    }

    public String getVerificadoPor() {
        return verificadoPor;
    }

    public void setVerificadoPor(String verificadoPor) {
        this.verificadoPor = verificadoPor;
    }

    public boolean isVerificado() {
        return ChecklistCabecalho.VALIDADA.equals(status);
    }

    /** Status do checklist; checklist sem cabecalho (antigo) conta como em andamento. */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : ChecklistCabecalho.EM_ANDAMENTO;
    }

    public String getStatusLabel() {
        return ChecklistCabecalho.statusLabel(status);
    }

    public boolean isCompleto() {
        return totalItens > 0 && itensChecados == totalItens;
    }
}
