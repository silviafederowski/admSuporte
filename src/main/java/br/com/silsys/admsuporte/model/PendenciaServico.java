package br.com.silsys.admsuporte.model;

import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PendenciaServico implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private Integer servicoId;
    private Integer prestadorId;
    private LocalDate data;
    private String nomeTecnico;
    private List<ItemPendenciaServico> itens = new ArrayList<>();

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String servicoDescricao;

    /** Preenchido via join na leitura; nao corresponde a uma coluna propria. */
    private String prestadorNome;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }

    public Integer getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Integer prestadorId) {
        this.prestadorId = prestadorId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    /** Formatada dd/mm/aa, para exibicao em tela. */
    public String getDataFormatada() {
        return DateFormatUtil.formatar(data);
    }

    /** Identificacao curta do registro para o log de operacoes (tecnico e prestador sao opcionais). */
    public String getResumoLog() {
        String resumo = servicoDescricao != null ? servicoDescricao : ("serviço " + servicoId);
        resumo += " em " + getDataFormatada();
        if (nomeTecnico != null && !nomeTecnico.trim().isEmpty()) {
            resumo += " (" + nomeTecnico.trim() + ")";
        }
        return resumo;
    }

    public String getNomeTecnico() {
        return nomeTecnico;
    }

    public void setNomeTecnico(String nomeTecnico) {
        this.nomeTecnico = nomeTecnico;
    }

    public List<ItemPendenciaServico> getItens() {
        return itens;
    }

    public void setItens(List<ItemPendenciaServico> itens) {
        this.itens = itens;
    }

    /** Pendente se ao menos um item estiver pendente; resolvido quando todos estiverem resolvidos. */
    public StatusPendencia getStatus() {
        for (ItemPendenciaServico item : itens) {
            if (item.getStatus() == StatusPendencia.PENDENTE) {
                return StatusPendencia.PENDENTE;
            }
        }
        return itens.isEmpty() ? StatusPendencia.PENDENTE : StatusPendencia.RESOLVIDO;
    }

    public String getServicoDescricao() {
        return servicoDescricao;
    }

    public void setServicoDescricao(String servicoDescricao) {
        this.servicoDescricao = servicoDescricao;
    }

    public String getPrestadorNome() {
        return prestadorNome;
    }

    public void setPrestadorNome(String prestadorNome) {
        this.prestadorNome = prestadorNome;
    }
}
