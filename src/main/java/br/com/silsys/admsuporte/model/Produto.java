package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;
    private Integer periodicidade;
    private PeriodicidadeUnidade unidadePeriodicidade;
    private LocalDate ultimaExecucao;
    private Integer ultimoFornecedorId;
    private BigDecimal valorPagoUltimaExecucao;
    private LocalDate dataAgendadaProximaExecucao;
    private Integer fornecedorProximaExecucaoId;
    private BigDecimal valorOrcadoProximaExecucao;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String ultimoFornecedorNome;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String fornecedorProximaExecucaoNome;

    /** Preenchido via fornecedor_produtos na leitura; somente para exibicao na tela. */
    private List<Fornecedor> fornecedoresQueOferecem = new ArrayList<>();

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

    public Integer getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(Integer periodicidade) {
        this.periodicidade = periodicidade;
    }

    public PeriodicidadeUnidade getUnidadePeriodicidade() {
        return unidadePeriodicidade;
    }

    public void setUnidadePeriodicidade(PeriodicidadeUnidade unidadePeriodicidade) {
        this.unidadePeriodicidade = unidadePeriodicidade;
    }

    public LocalDate getUltimaExecucao() {
        return ultimaExecucao;
    }

    public void setUltimaExecucao(LocalDate ultimaExecucao) {
        this.ultimaExecucao = ultimaExecucao;
    }

    public Integer getUltimoFornecedorId() {
        return ultimoFornecedorId;
    }

    public void setUltimoFornecedorId(Integer ultimoFornecedorId) {
        this.ultimoFornecedorId = ultimoFornecedorId;
    }

    public String getUltimoFornecedorNome() {
        return ultimoFornecedorNome;
    }

    public void setUltimoFornecedorNome(String ultimoFornecedorNome) {
        this.ultimoFornecedorNome = ultimoFornecedorNome;
    }

    public BigDecimal getValorPagoUltimaExecucao() {
        return valorPagoUltimaExecucao;
    }

    public void setValorPagoUltimaExecucao(BigDecimal valorPagoUltimaExecucao) {
        this.valorPagoUltimaExecucao = valorPagoUltimaExecucao;
    }

    public LocalDate getDataAgendadaProximaExecucao() {
        return dataAgendadaProximaExecucao;
    }

    public void setDataAgendadaProximaExecucao(LocalDate dataAgendadaProximaExecucao) {
        this.dataAgendadaProximaExecucao = dataAgendadaProximaExecucao;
    }

    public Integer getFornecedorProximaExecucaoId() {
        return fornecedorProximaExecucaoId;
    }

    public void setFornecedorProximaExecucaoId(Integer fornecedorProximaExecucaoId) {
        this.fornecedorProximaExecucaoId = fornecedorProximaExecucaoId;
    }

    public String getFornecedorProximaExecucaoNome() {
        return fornecedorProximaExecucaoNome;
    }

    public void setFornecedorProximaExecucaoNome(String fornecedorProximaExecucaoNome) {
        this.fornecedorProximaExecucaoNome = fornecedorProximaExecucaoNome;
    }

    public BigDecimal getValorOrcadoProximaExecucao() {
        return valorOrcadoProximaExecucao;
    }

    public void setValorOrcadoProximaExecucao(BigDecimal valorOrcadoProximaExecucao) {
        this.valorOrcadoProximaExecucao = valorOrcadoProximaExecucao;
    }

    public List<Fornecedor> getFornecedoresQueOferecem() {
        return fornecedoresQueOferecem;
    }

    public void setFornecedoresQueOferecem(List<Fornecedor> fornecedoresQueOferecem) {
        this.fornecedoresQueOferecem = fornecedoresQueOferecem != null ? fornecedoresQueOferecem : new ArrayList<>();
    }

    /** Calculado em tela: data prevista da proxima execucao (ultima execucao + periodicidade). Null se nunca executado. */
    public LocalDate getProximaExecucao() {
        if (ultimaExecucao == null || unidadePeriodicidade == null || periodicidade == null) {
            return null;
        }
        switch (unidadePeriodicidade) {
            case DIA:
                return ultimaExecucao.plusDays(periodicidade);
            case MES:
                return ultimaExecucao.plusMonths(periodicidade);
            case ANO:
                return ultimaExecucao.plusYears(periodicidade);
            case POR_DEMANDA:
            default:
                return null;
        }
    }

    /** Calculado em tela: dias entre hoje e a proxima execucao (negativo se atrasado). Null se nunca executado. */
    public Long getDiasFaltantes() {
        LocalDate proximaExecucao = getProximaExecucao();
        if (proximaExecucao == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), proximaExecucao);
    }

    /** Calculado em tela: nunca executado ou proxima execucao ja passou. */
    public boolean isAtrasado() {
        if (unidadePeriodicidade == PeriodicidadeUnidade.POR_DEMANDA) {
            return false;
        }
        if (ultimaExecucao == null) {
            return true;
        }
        Long diasFaltantes = getDiasFaltantes();
        return diasFaltantes != null && diasFaltantes < 0;
    }
}
