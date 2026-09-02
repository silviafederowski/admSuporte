package br.com.silsys.admsuporte.model;

/**
 * Se e contratado (fornecido de forma continua/recorrente) ou solicitado sob demanda
 * quando necessario (coluna "tipo" de servicos e, com o mesmo significado, de prestadores).
 */
public enum TipoServico {
    CONTRATADO,
    SOLICITAR;

    public static TipoServico fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "contratado":
                return CONTRATADO;
            case "solicitar":
                return SOLICITAR;
            default:
                throw new IllegalArgumentException("Tipo de servico invalido: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case CONTRATADO:
                return "contratado";
            case SOLICITAR:
                return "solicitar";
            default:
                throw new IllegalStateException("Tipo de servico nao mapeado: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP. */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case CONTRATADO:
                return "Contratado";
            case SOLICITAR:
                return "Solicitar";
            default:
                throw new IllegalStateException("Tipo de servico nao mapeado: " + this);
        }
    }
}
