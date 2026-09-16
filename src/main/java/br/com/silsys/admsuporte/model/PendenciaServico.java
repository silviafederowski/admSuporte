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

    /** Linhas nao vazias da descricao (uma por linha do textarea), para exibir em tabela sem linhas em branco. */
    public List<String> getDescricaoOcorrenciaLinhas() {
        List<String> linhas = new ArrayList<>();
        if (descricaoOcorrencia == null) {
            return linhas;
        }
        for (String linha : descricaoOcorrencia.split("\\r?\\n")) {
            String aparada = linha.trim();
            if (!aparada.isEmpty()) {
                linhas.add(aparada);
            }
        }
        return linhas;
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
