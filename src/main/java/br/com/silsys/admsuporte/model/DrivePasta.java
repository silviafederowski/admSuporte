package br.com.silsys.admsuporte.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Uma subpasta (a qualquer profundidade) dentro de "ParaWeb" no Google Drive, com seus arquivos. */
public class DrivePasta implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;
    private List<DriveArquivo> arquivos = new ArrayList<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<DriveArquivo> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<DriveArquivo> arquivos) {
        this.arquivos = arquivos != null ? arquivos : new ArrayList<>();
    }
}
