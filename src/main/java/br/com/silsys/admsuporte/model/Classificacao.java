package br.com.silsys.admsuporte.model;

/** Classificacao de um prestador (coluna prestadores.classificacao). */
public enum Classificacao {
    MUITO_BOM,
    BOM,
    MEDIO,
    RUIM,
    MUITO_RUIM;

    public static Classificacao fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "muito_bom":
                return MUITO_BOM;
            case "bom":
                return BOM;
            case "medio":
                return MEDIO;
            case "ruim":
                return RUIM;
            case "muito_ruim":
                return MUITO_RUIM;
            default:
                throw new IllegalArgumentException("Classificacao invalida: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case MUITO_BOM:
                return "muito_bom";
            case BOM:
                return "bom";
            case MEDIO:
                return "medio";
            case RUIM:
                return "ruim";
            case MUITO_RUIM:
                return "muito_ruim";
            default:
                throw new IllegalStateException("Classificacao nao mapeada: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP (${p.classificacao.dbValue}). */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case MUITO_BOM:
                return "Muito bom";
            case BOM:
                return "Bom";
            case MEDIO:
                return "Médio";
            case RUIM:
                return "Ruim";
            case MUITO_RUIM:
                return "Muito ruim";
            default:
                throw new IllegalStateException("Classificacao nao mapeada: " + this);
        }
    }
}
