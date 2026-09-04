package br.com.silsys.admsuporte.model;

import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.io.Serializable;
import java.time.LocalDate;

public class PendenciaServico implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private Integer servicoId;
    private Integer prestadorId;
    private LocalDate data;
    private String nomeTecnico;
    private String descricaoOcorrencia;
    private StatusPendencia status;

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

    public String getNomeTecnico() {
        return nomeTecnico;
    }

    public void setNomeTecnico(String nomeTecnico) {
        this.nomeTecnico = nomeTecnico;
    }

    public String getDescricaoOcorrencia() {
        return descricaoOcorrencia;
    }

    public void setDescricaoOcorrencia(String descricaoOcorrencia) {
        this.descricaoOcorrencia = descricaoOcorrencia;
    }

    public StatusPendencia getStatus() {
        return status;
    }

    public void setStatus(StatusPendencia status) {
        this.status = status;
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
