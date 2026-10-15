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
    private Integer servicoId;
    private Integer prestadorId;
    private StatusPendencia status = StatusPendencia.PENDENTE;
    private LocalDate dataStatus;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String servicoDescricao;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String prestadorNome;

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

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public Integer getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Integer prestadorId) {
        this.prestadorId = prestadorId;
    }

    public StatusPendencia getStatus() {
        return status;
    }

    public void setStatus(StatusPendencia status) {
        this.status = status != null ? status : StatusPendencia.PENDENTE;
    }

    public LocalDate getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(LocalDate dataStatus) {
        this.dataStatus = dataStatus;
    }

    /** Formatada dd/mm/aa, para exibicao em tela. */
    public String getDataStatusFormatada() {
        return DateFormatUtil.formatar(dataStatus);
    }

    public String getServicoDescricao() {
        return servicoDescricao;
    }

    public void setServicoDescricao(String servicoDescricao) {
        this.servicoDescricao = servicoDescricao;
    }

    public String getPrestadorNome() {
        return prestadorNome;
    }

    public void setPrestadorNome(String prestadorNome) {
        this.prestadorNome = prestadorNome;
    }

    /** Compromisso pendente marcado para hoje (os ja resolvidos nao precisam mais de aviso). */
    public boolean isHoje() {
        return status != StatusPendencia.RESOLVIDO && data != null && data.isEqual(LocalDate.now());
    }
}
