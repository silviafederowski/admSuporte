package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/** Um arquivo listado na pasta do Google Drive (ver GoogleDriveClient). */
public class DriveArquivo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private String mimeType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /** Rotulo amigavel do tipo de arquivo, para exibicao na lista. */
    public String getTipo() {
        if (mimeType == null) {
            return "Arquivo";
        }
        if (mimeType.equals("application/pdf")) {
            return "PDF";
        }
        if (mimeType.startsWith("image/")) {
            return "Imagem";
        }
        if (mimeType.equals("application/vnd.google-apps.document")) {
            return "Documento";
        }
        if (mimeType.equals("application/vnd.google-apps.spreadsheet")) {
            return "Planilha";
        }
        if (mimeType.equals("application/vnd.google-apps.presentation")) {
            return "Apresentação";
        }
        if (mimeType.contains("word")) {
            return "Word";
        }
        if (mimeType.contains("sheet") || mimeType.contains("excel")) {
            return "Excel";
        }
        return "Arquivo";
    }
}
