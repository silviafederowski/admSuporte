package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Uma pendencia (descricao + situacao) dentro de um registro do historico de servico de uma data. */
public class ItemPendenciaServico implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;
    private String pontoFocal;
    private StatusPendencia status = StatusPendencia.PENDENTE;

    public ItemPendenciaServico() {
    }

    public ItemPendenciaServico(String descricao, String pontoFocal, StatusPendencia status) {
        this.descricao = descricao;
        this.pontoFocal = pontoFocal;
        this.status = status;
    }

    /** Responsavel pela pendencia (ate 10 caracteres). */
    public String getPontoFocal() {
        return pontoFocal;
    }

    public void setPontoFocal(String pontoFocal) {
        this.pontoFocal = pontoFocal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusPendencia getStatus() {
        return status;
    }

    public void setStatus(StatusPendencia status) {
        this.status = status;
    }
}
