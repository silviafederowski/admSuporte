package br.com.silsys.admsuporte.model;

/** Situacao de um registro do historico de servico (coluna "status" de pendencias_servico). */
public enum StatusPendencia {
    PENDENTE,
    RESOLVIDO;

    public static StatusPendencia fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "pendente":
                return PENDENTE;
            case "resolvido":
                return RESOLVIDO;
            default:
                throw new IllegalArgumentException("Status de pendencia invalido: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case PENDENTE:
                return "pendente";
            case RESOLVIDO:
                return "resolvido";
            default:
                throw new IllegalStateException("Status de pendencia nao mapeado: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP. */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case PENDENTE:
                return "Pendente";
            case RESOLVIDO:
                return "Resolvido";
            default:
                throw new IllegalStateException("Status de pendencia nao mapeado: " + this);
        }
    }
}
