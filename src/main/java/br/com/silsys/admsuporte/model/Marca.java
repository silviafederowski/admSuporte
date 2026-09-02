package br.com.silsys.admsuporte.model;

import java.io.Serializable;

public class Marca implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descricao;

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
}
