package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Uma celula da grade de Sorteio de vagas: vaga(s) atribuida(s) a uma unidade e a cor do tipo. */
public class SorteioVagaCell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigos;
    private final String cssClass;
    private final String unidadeCodigo;

    public SorteioVagaCell(String codigos, String cssClass) {
        this(codigos, cssClass, "");
    }

    /**
     * @param unidadeCodigo codigo da unidade dona da celula (independente do que e exibido em
     *                      codigos), usado para identificar a celula via atributo data-* no JS
     *                      do sorteio, mesmo depois que o conteudo exibido e sobrescrito.
     */
    public SorteioVagaCell(String codigos, String cssClass, String unidadeCodigo) {
        this.codigos = codigos;
        this.cssClass = cssClass;
        this.unidadeCodigo = unidadeCodigo;
    }

    public String getCodigos() {
        return codigos;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getUnidadeCodigo() {
        return unidadeCodigo;
    }
}
