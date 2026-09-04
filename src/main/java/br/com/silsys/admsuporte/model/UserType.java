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

    /** Acima deste nivel (ou seja, condomino = 15), o acesso e bloqueado: usado onde o condomino
     *  nao deve nem consultar a tela (alerta de atraso, cadastro de usuarios). */
    public static final int NIVEL_MAXIMO_SEM_CONDOMINO = 10;

    /** Ate este nivel (inclusive), o usuario recebe o alerta de servicos atrasados ao logar. Condomino (15) fica de fora. */
    public static final int NIVEL_MAXIMO_ALERTA_ATRASO = NIVEL_MAXIMO_SEM_CONDOMINO;

    /** Ate este nivel (inclusive), o usuario recebe o alerta de servicos agendados para hoje ao logar (nivel < 6: so administrador e zelador). */
    public static final int NIVEL_MAXIMO_ALERTA_AGENDADO = 5;

    /** Ate este nivel (inclusive), a tela de Documentos tambem mostra os arquivos da subpasta "procedimentos" dentro de "ParaWeb" (nivel < 6: so administrador e zelador). */
    public static final int NIVEL_MAXIMO_SUBPASTA_PROCEDIMENTOS = 5;

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

    /**
     * Nivel com 2 digitos (00, 01, 05, 10...), so para exibicao nas listas: o filtro de coluna
     * (busca por trecho) confunde "0" com "10"/"20" e "1" com "10"/"15" quando o numero tem so
     * 1 digito. O valor real (int) usado em toda a logica de autorizacao nao muda.
     */
    public String getNivelFormatado() {
        return String.format("%02d", nivel);
    }
}
