package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
    private Integer estoqueMinimo;
    private Integer estoqueAtual;
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

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public void setEstoqueAtual(Integer estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    /**
     * Quantidade sugerida para compra, na unidade de medida do produto (ex.: duzias). Estoque
     * minimo e estoque atual sao inteiros digitados direto em unidades; estoque ideal e digitado
     * na unidade de medida do produto. Calculada quando estoqueAtual for menor que estoqueMinimo:
     * arredonda para cima (inteiro acima) o resultado de ((estoqueIdeal x conversaoUnidades) -
     * estoqueAtual) / conversaoUnidades. Recalculada em tela via JS quando estoque ideal/minimo/
     * atual/conversao mudam, mas pode ser sobrescrita manualmente pelo usuario antes de salvar.
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
