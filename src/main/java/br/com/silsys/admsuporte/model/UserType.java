package br.com.silsys.admsuporte.model;

import java.io.Serializable;

public class UserType implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ADMINISTRADOR = "administrador";
    public static final String ZELADOR = "zelador";
    public static final String CONDOMINO = "condomino";
    public static final String COLABORADOR = "colaborador";

    /** Acima deste nivel, o usuario so pode consultar telas; escrita (criar/editar/excluir) fica bloqueada. */
    public static final int NIVEL_MAXIMO_ESCRITA = 9;

    /** Ate este nivel (inclusive), o usuario recebe o alerta de servicos atrasados ao logar. */
    public static final int NIVEL_MAXIMO_ALERTA_ATRASO = 15;

    private int id;
    private String name;
    private int nivel;

    public UserType() {
    }

    public UserType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public UserType(int id, String name, int nivel) {
        this.id = id;
        this.name = name;
        this.nivel = nivel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
}
