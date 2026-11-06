package br.com.silsys.admsuporte.model;

/** Prioridade de um compromisso da agenda (coluna "prioridade" de agenda): alta, media ou baixa. */
public enum Prioridade {
    ALTA,
    MEDIA,
    BAIXA;

    public static Prioridade fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "alta":
                return ALTA;
            case "media":
                return MEDIA;
            case "baixa":
                return BAIXA;
            default:
                throw new IllegalArgumentException("Prioridade invalida: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case ALTA:
                return "alta";
            case MEDIA:
                return "media";
            case BAIXA:
                return "baixa";
            default:
                throw new IllegalStateException("Prioridade nao mapeada: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP. */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case ALTA:
                return "Alta";
            case MEDIA:
                return "Média";
            case BAIXA:
                return "Baixa";
            default:
                throw new IllegalStateException("Prioridade nao mapeada: " + this);
        }
    }
}
