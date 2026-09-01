package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Prestador implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nomeRazaoSocial;
    private String email;
    private String contato1Nome;
    private String contato1Cargo;
    private String contato1Telefone;
    private String contato2Nome;
    private String contato2Cargo;
    private String contato2Telefone;
    private String contato3Nome;
    private String contato3Cargo;
    private String contato3Telefone;
    private Classificacao classificacao;
    private TipoContratacao regularOuContratado;
    private String observacao;
    private List<Integer> servicoIds = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeRazaoSocial() {
        return nomeRazaoSocial;
    }

    public void setNomeRazaoSocial(String nomeRazaoSocial) {
        this.nomeRazaoSocial = nomeRazaoSocial;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContato1Nome() {
        return contato1Nome;
    }

    public void setContato1Nome(String contato1Nome) {
        this.contato1Nome = contato1Nome;
    }

    public String getContato1Cargo() {
        return contato1Cargo;
    }

    public void setContato1Cargo(String contato1Cargo) {
        this.contato1Cargo = contato1Cargo;
    }

    public String getContato1Telefone() {
        return contato1Telefone;
    }

    public void setContato1Telefone(String contato1Telefone) {
        this.contato1Telefone = contato1Telefone;
    }

    public String getContato2Nome() {
        return contato2Nome;
    }

    public void setContato2Nome(String contato2Nome) {
        this.contato2Nome = contato2Nome;
    }

    public String getContato2Cargo() {
        return contato2Cargo;
    }

    public void setContato2Cargo(String contato2Cargo) {
        this.contato2Cargo = contato2Cargo;
    }

    public String getContato2Telefone() {
        return contato2Telefone;
    }

    public void setContato2Telefone(String contato2Telefone) {
        this.contato2Telefone = contato2Telefone;
    }

    public String getContato3Nome() {
        return contato3Nome;
    }

    public void setContato3Nome(String contato3Nome) {
        this.contato3Nome = contato3Nome;
    }

    public String getContato3Cargo() {
        return contato3Cargo;
    }

    public void setContato3Cargo(String contato3Cargo) {
        this.contato3Cargo = contato3Cargo;
    }

    public String getContato3Telefone() {
        return contato3Telefone;
    }

    public void setContato3Telefone(String contato3Telefone) {
        this.contato3Telefone = contato3Telefone;
    }

    public Classificacao getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(Classificacao classificacao) {
        this.classificacao = classificacao;
    }

    public TipoContratacao getRegularOuContratado() {
        return regularOuContratado;
    }

    public void setRegularOuContratado(TipoContratacao regularOuContratado) {
        this.regularOuContratado = regularOuContratado;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Integer> getServicoIds() {
        return servicoIds;
    }

    public void setServicoIds(List<Integer> servicoIds) {
        this.servicoIds = servicoIds != null ? servicoIds : new ArrayList<>();
    }
}
