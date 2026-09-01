package br.com.silsys.admsuporte.model;

/** Se o prestador e regular (permanente) ou contratado por demanda (coluna prestadores.regular_ou_contratado). */
public enum TipoContratacao {
    REGULAR,
    CONTRATADO;

    public static TipoContratacao fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "regular":
                return REGULAR;
            case "contratado":
                return CONTRATADO;
            default:
                throw new IllegalArgumentException("Tipo de contratacao invalido: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case REGULAR:
                return "regular";
            case CONTRATADO:
                return "contratado";
            default:
                throw new IllegalStateException("Tipo de contratacao nao mapeado: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP. */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case REGULAR:
                return "Regular";
            case CONTRATADO:
                return "Contratado";
            default:
                throw new IllegalStateException("Tipo de contratacao nao mapeado: " + this);
        }
    }
}
