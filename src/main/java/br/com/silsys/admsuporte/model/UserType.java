package br.com.silsys.admsuporte.model;

import java.io.Serializable;

public class UserType implements Serializable {

    private static final long serialVersionUID = 1L;

    // Nomes exatos dos tipos padrao em user_types (o SchemaInitializer garante que existam).
    public static final String ADMINISTRADOR = "administrador(a)";
    public static final String ZELADOR = "zelador(a)";
    public static final String CONDOMINO = "condômino(a)";
    public static final String PORTEIRO = "porteiro(a)";

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

    /** Tipos que, no Checklist, veem os itens de todos os tipos e os checklists de todos os usuarios. */
    public static final java.util.Set<String> TIPOS_CHECKLIST_TODOS = java.util.Collections.unmodifiableSet(
            new java.util.HashSet<>(java.util.Arrays.asList(ADMINISTRADOR, ZELADOR, "síndico(a)")));

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
