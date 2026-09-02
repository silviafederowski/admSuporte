package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Unidade do condominio (tabela unidades, importada de sistema externo; somente leitura por aqui). */
public class Unidade implements Serializable {

    private static final long serialVersionUID = 1L;

    private int chave;
    private String codigo;
    private Integer quantasVagas;

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
}
