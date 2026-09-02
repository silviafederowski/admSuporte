package br.com.silsys.admsuporte.model;

import java.io.Serializable;

public class Veiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int unidadeChave;
    private int corId;
    private int marcaId;
    private int modeloId;
    private String placas;

    // Campos denormalizados para exibicao na lista (ver VeiculoDao.listAllComDetalhes).
    private String unidadeCodigo;
    private String corDescricao;
    private String marcaDescricao;
    private String modeloDescricao;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUnidadeChave() {
        return unidadeChave;
    }

    public void setUnidadeChave(int unidadeChave) {
        this.unidadeChave = unidadeChave;
    }

    public int getCorId() {
        return corId;
    }

    public void setCorId(int corId) {
        this.corId = corId;
    }

    public int getMarcaId() {
        return marcaId;
    }

    public void setMarcaId(int marcaId) {
        this.marcaId = marcaId;
    }

    public int getModeloId() {
        return modeloId;
    }

    public void setModeloId(int modeloId) {
        this.modeloId = modeloId;
    }

    public String getPlacas() {
        return placas;
    }

    public void setPlacas(String placas) {
        this.placas = placas;
    }

    public String getUnidadeCodigo() {
        return unidadeCodigo;
    }

    public void setUnidadeCodigo(String unidadeCodigo) {
        this.unidadeCodigo = unidadeCodigo;
    }

    public String getCorDescricao() {
        return corDescricao;
    }

    public void setCorDescricao(String corDescricao) {
        this.corDescricao = corDescricao;
    }

    public String getMarcaDescricao() {
        return marcaDescricao;
    }

    public void setMarcaDescricao(String marcaDescricao) {
        this.marcaDescricao = marcaDescricao;
    }

    public String getModeloDescricao() {
        return modeloDescricao;
    }

    public void setModeloDescricao(String modeloDescricao) {
        this.modeloDescricao = modeloDescricao;
    }
}
