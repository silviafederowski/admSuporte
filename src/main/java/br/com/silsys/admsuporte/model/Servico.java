package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Servico implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;
    private Integer periodicidade;
    private PeriodicidadeUnidade unidadePeriodicidade;
    private LocalDate ultimaExecucao;
    private Integer ultimoPrestadorId;
    private BigDecimal valorPagoUltimaExecucao;
    private LocalDate dataAgendadaProximaExecucao;
    private Integer prestadorProximaExecucaoId;
    private BigDecimal valorOrcadoProximaExecucao;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String ultimoPrestadorNome;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String prestadorProximaExecucaoNome;

    /** Preenchido via prestador_servicos na leitura; somente para exibicao na tela. */
    private List<Prestador> prestadoresQueOferecem = new ArrayList<>();

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

    public Integer getUltimoPrestadorId() {
        return ultimoPrestadorId;
    }

    public void setUltimoPrestadorId(Integer ultimoPrestadorId) {
        this.ultimoPrestadorId = ultimoPrestadorId;
    }

    public String getUltimoPrestadorNome() {
        return ultimoPrestadorNome;
    }

    public void setUltimoPrestadorNome(String ultimoPrestadorNome) {
        this.ultimoPrestadorNome = ultimoPrestadorNome;
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

    public Integer getPrestadorProximaExecucaoId() {
        return prestadorProximaExecucaoId;
    }

    public void setPrestadorProximaExecucaoId(Integer prestadorProximaExecucaoId) {
        this.prestadorProximaExecucaoId = prestadorProximaExecucaoId;
    }

    public String getPrestadorProximaExecucaoNome() {
        return prestadorProximaExecucaoNome;
    }

    public void setPrestadorProximaExecucaoNome(String prestadorProximaExecucaoNome) {
        this.prestadorProximaExecucaoNome = prestadorProximaExecucaoNome;
    }

    public BigDecimal getValorOrcadoProximaExecucao() {
        return valorOrcadoProximaExecucao;
    }

    public void setValorOrcadoProximaExecucao(BigDecimal valorOrcadoProximaExecucao) {
        this.valorOrcadoProximaExecucao = valorOrcadoProximaExecucao;
    }

    public List<Prestador> getPrestadoresQueOferecem() {
        return prestadoresQueOferecem;
    }

    public void setPrestadoresQueOferecem(List<Prestador> prestadoresQueOferecem) {
        this.prestadoresQueOferecem = prestadoresQueOferecem != null ? prestadoresQueOferecem : new ArrayList<>();
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
