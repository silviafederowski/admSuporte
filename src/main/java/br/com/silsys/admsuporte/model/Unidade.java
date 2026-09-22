package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Unidade do condominio (tabela unidades, importada de sistema externo; somente leitura por aqui). */
public class Unidade implements Serializable {

    private static final long serialVersionUID = 1L;

    private int chave;
    private String codigo;
    private Integer quantasVagas;
    private boolean candidataIdoso;
    private Integer vagasGaragem;

    public int getChave() {
        return chave;
    }

    public void setChave(int chave) {
        this.chave = chave;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getQuantasVagas() {
        return quantasVagas;
    }

    public void setQuantasVagas(Integer quantasVagas) {
        this.quantasVagas = quantasVagas;
    }

    public boolean isCandidataIdoso() {
        return candidataIdoso;
    }

    public void setCandidataIdoso(boolean candidataIdoso) {
        this.candidataIdoso = candidataIdoso;
    }

    /** Quantas vagas de garagem a unidade tem: 1 ou 2. */
    public Integer getVagasGaragem() {
        return vagasGaragem;
    }

    public void setVagasGaragem(Integer vagasGaragem) {
        this.vagasGaragem = vagasGaragem;
    }
}
