package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;
    private Integer tipoId;
    private UnidadeMedidaProduto unidade;
    private BigDecimal conversaoUnidades;
    private BigDecimal estoqueIdeal;
    private BigDecimal estoqueMinimo;
    private BigDecimal estoqueAtual;
    private BigDecimal comprar;
    private Integer ultimoFornecedorId;
    private BigDecimal valorUltimaCompra;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String tipoDescricao;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String ultimoFornecedorNome;

    /** Preenchido via fornecedor_produtos_tipo na leitura; somente para exibicao na tela. */
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

    public Integer getTipoId() {
        return tipoId;
    }

    public void setTipoId(Integer tipoId) {
        this.tipoId = tipoId;
    }

    public String getTipoDescricao() {
        return tipoDescricao;
    }

    public void setTipoDescricao(String tipoDescricao) {
        this.tipoDescricao = tipoDescricao;
    }

    public UnidadeMedidaProduto getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedidaProduto unidade) {
        this.unidade = unidade;
    }

    /** Fator de conversao da unidade de medida deste produto para unidades (ex.: duzia = 12). */
    public BigDecimal getConversaoUnidades() {
        return conversaoUnidades;
    }

    public void setConversaoUnidades(BigDecimal conversaoUnidades) {
        this.conversaoUnidades = conversaoUnidades;
    }

    public BigDecimal getEstoqueIdeal() {
        return estoqueIdeal;
    }

    public void setEstoqueIdeal(BigDecimal estoqueIdeal) {
        this.estoqueIdeal = estoqueIdeal;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(BigDecimal estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    /**
     * Estoque minimo convertido para unidades (estoqueMinimo x conversaoUnidades), para exibicao.
     * Estoque ideal e estoque minimo sao digitados na unidade de medida do produto (ex.: duzias);
     * estoque atual e digitado direto em unidades (ver getComprar).
     */
    public BigDecimal getEstoqueMinimoUnidades() {
        if (estoqueMinimo == null || conversaoUnidades == null) {
            return null;
        }
        return estoqueMinimo.multiply(conversaoUnidades).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Quantidade sugerida para compra, na unidade de medida do produto (ex.: duzias). Calculada
     * quando estoqueAtual (digitado em unidades) for menor que estoqueMinimo x conversaoUnidades:
     * arredonda ao inteiro mais proximo de (estoqueIdeal - (estoqueAtual / conversaoUnidades)).
     * Recalculada em tela via JS quando estoque ideal/minimo/atual/conversao mudam, mas pode ser
     * sobrescrita manualmente pelo usuario antes de salvar.
     */
    public BigDecimal getComprar() {
        return comprar;
    }

    public void setComprar(BigDecimal comprar) {
        this.comprar = comprar;
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

    public BigDecimal getValorUltimaCompra() {
        return valorUltimaCompra;
    }

    public void setValorUltimaCompra(BigDecimal valorUltimaCompra) {
        this.valorUltimaCompra = valorUltimaCompra;
    }

    public List<Fornecedor> getFornecedoresQueOferecem() {
        return fornecedoresQueOferecem;
    }

    public void setFornecedoresQueOferecem(List<Fornecedor> fornecedoresQueOferecem) {
        this.fornecedoresQueOferecem = fornecedoresQueOferecem != null ? fornecedoresQueOferecem : new ArrayList<>();
    }
}
