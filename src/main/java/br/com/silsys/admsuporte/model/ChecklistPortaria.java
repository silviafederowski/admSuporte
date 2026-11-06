package br.com.silsys.admsuporte.model;

import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Um item de um checklist da portaria (tabela checklist_portaria). */
public class ChecklistPortaria implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter ALTERADO_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    private int id;
    private LocalDate data;
    private LocalTime hora;
    private String usuario;
    private int itemId;
    private boolean checado;
    private String observacao;
    /** Validacao do item: null = ainda nao validado; true = confirmado; false = nao confirmado (ver validacaoObs). */
    private Boolean validado;
    private String validacaoObs;
    private LocalDateTime alteradoEm;

    /** Preenchido via join com checklist_itens na leitura. */
    private String itemDescricao;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDataFormatada() {
        return DateFormatUtil.formatar(data);
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getHoraFormatada() {
        return DateFormatUtil.formatarHora(hora);
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public boolean isChecado() {
        return checado;
    }

    public void setChecado(boolean checado) {
        this.checado = checado;
    }

    /** Preenchidos so por ChecklistPortariaDao.atualizar, para o log saber o que mudou na linha. */
    private boolean checadoMudou;
    private boolean observacaoMudou;

    public boolean isChecadoMudou() {
        return checadoMudou;
    }

    public void setChecadoMudou(boolean checadoMudou) {
        this.checadoMudou = checadoMudou;
    }

    public boolean isObservacaoMudou() {
        return observacaoMudou;
    }

    public void setObservacaoMudou(boolean observacaoMudou) {
        this.observacaoMudou = observacaoMudou;
    }

    public Boolean getValidado() {
        return validado;
    }

    public void setValidado(Boolean validado) {
        this.validado = validado;
    }

    public String getValidacaoObs() {
        return validacaoObs;
    }

    public void setValidacaoObs(String validacaoObs) {
        this.validacaoObs = validacaoObs;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getAlteradoEm() {
        return alteradoEm;
    }

    public void setAlteradoEm(LocalDateTime alteradoEm) {
        this.alteradoEm = alteradoEm;
    }

    public String getAlteradoEmFormatado() {
        return alteradoEm != null ? alteradoEm.format(ALTERADO_FORMATTER) : "";
    }

    public String getItemDescricao() {
        return itemDescricao;
    }

    public void setItemDescricao(String itemDescricao) {
        this.itemDescricao = itemDescricao;
    }
}
