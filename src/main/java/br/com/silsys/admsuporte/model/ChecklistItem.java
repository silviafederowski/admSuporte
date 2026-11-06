package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Item de checklist (tabela checklist_itens): o que verificar e qual tipo de usuario faz a verificacao. */
public class ChecklistItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int userTypeId;
    private String descricao;

    /** Preenchido via join com user_types na leitura. */
    private String userTypeName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(int userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getUserTypeName() {
        return userTypeName;
    }

    public void setUserTypeName(String userTypeName) {
        this.userTypeName = userTypeName;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
