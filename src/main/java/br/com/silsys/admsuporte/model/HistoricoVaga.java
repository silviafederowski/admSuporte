package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Linha do cruzamento entre historico de vagas (histvagas), unidades e vagas. Somente leitura. */
public class HistoricoVaga implements Serializable {

    private static final long serialVersionUID = 1L;

    private int chave;
    private Integer ano;
    private String internaExternaHistorico;
    private String observacaoHistorico;

    private Integer unidadeChave;
    private String unidadeCodigo;
    private Integer unidadeQuantasVagas;

    private String vagaCodigo;
    private String vagaPavimento;
    private String vagaInternaExterna;
    private String vagaIndependente;
    private Integer vagaQuantasVagas;
    private String vagaEspecial;
    private String vagaObservacao;

    public int getChave() {
        return chave;
    }

    public void setChave(int chave) {
        this.chave = chave;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getInternaExternaHistorico() {
        return internaExternaHistorico;
    }

    public void setInternaExternaHistorico(String internaExternaHistorico) {
        this.internaExternaHistorico = internaExternaHistorico;
    }

    public String getObservacaoHistorico() {
        return observacaoHistorico;
    }

    public void setObservacaoHistorico(String observacaoHistorico) {
        this.observacaoHistorico = observacaoHistorico;
    }

    public Integer getUnidadeChave() {
        return unidadeChave;
    }

    public void setUnidadeChave(Integer unidadeChave) {
        this.unidadeChave = unidadeChave;
    }

    public String getUnidadeCodigo() {
        return unidadeCodigo;
    }

    public void setUnidadeCodigo(String unidadeCodigo) {
        this.unidadeCodigo = unidadeCodigo;
    }

    public Integer getUnidadeQuantasVagas() {
        return unidadeQuantasVagas;
    }

    public void setUnidadeQuantasVagas(Integer unidadeQuantasVagas) {
        this.unidadeQuantasVagas = unidadeQuantasVagas;
    }

    public String getVagaCodigo() {
        return vagaCodigo;
    }

    public void setVagaCodigo(String vagaCodigo) {
        this.vagaCodigo = vagaCodigo;
    }

    public String getVagaPavimento() {
        return vagaPavimento;
    }

    public void setVagaPavimento(String vagaPavimento) {
        this.vagaPavimento = vagaPavimento;
    }

    public String getVagaInternaExterna() {
        return vagaInternaExterna;
    }

    public void setVagaInternaExterna(String vagaInternaExterna) {
        this.vagaInternaExterna = vagaInternaExterna;
    }

    public String getVagaIndependente() {
        return vagaIndependente;
    }

    public void setVagaIndependente(String vagaIndependente) {
        this.vagaIndependente = vagaIndependente;
    }

    public Integer getVagaQuantasVagas() {
        return vagaQuantasVagas;
    }

    public void setVagaQuantasVagas(Integer vagaQuantasVagas) {
        this.vagaQuantasVagas = vagaQuantasVagas;
    }

    public String getVagaEspecial() {
        return vagaEspecial;
    }

    public void setVagaEspecial(String vagaEspecial) {
        this.vagaEspecial = vagaEspecial;
    }

    public String getVagaObservacao() {
        return vagaObservacao;
    }

    public void setVagaObservacao(String vagaObservacao) {
        this.vagaObservacao = vagaObservacao;
    }
}
