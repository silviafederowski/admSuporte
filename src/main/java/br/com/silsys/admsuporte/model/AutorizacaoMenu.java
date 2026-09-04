package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/**
 * Uma linha da tabela autorizacoes_menu: define se o nivel informado enxerga a opcao de menu
 * "telaCodigo" e se pode so consultar ou tambem editar. Sem linha para (telaCodigo, nivel), a
 * opcao nem aparece no menu nem pode ser acessada diretamente pela URL (ver MenuAutorizacaoFilter).
 */
public class AutorizacaoMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String telaCodigo;
    private String telaDescricao;
    private Integer nivel;
    private String tipoAcesso;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelaCodigo() {
        return telaCodigo;
    }

    public void setTelaCodigo(String telaCodigo) {
        this.telaCodigo = telaCodigo;
    }

    public String getTelaDescricao() {
        return telaDescricao;
    }

    public void setTelaDescricao(String telaDescricao) {
        this.telaDescricao = telaDescricao;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }

    /**
     * Nivel com 2 digitos (00, 01, 05, 10...), so para exibicao nas listas: o filtro de coluna
     * (busca por trecho) confunde "0" com "10"/"20" e "1" com "10"/"15" quando o numero tem so
     * 1 digito. O valor real (int) usado em toda a logica de autorizacao nao muda.
     */
    public String getNivelFormatado() {
        return nivel == null ? "" : String.format("%02d", nivel);
    }
}
