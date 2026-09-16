package br.com.silsys.admsuporte.model;

/** Unidade de medida de um produto (coluna produtos.unidade). */
public enum UnidadeMedidaProduto {
    UNIDADES,
    DUZIAS,
    KGS,
    LITROS;

    public static UnidadeMedidaProduto fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "unidades":
                return UNIDADES;
            case "duzias":
                return DUZIAS;
            case "kgs":
                return KGS;
            case "litros":
                return LITROS;
            default:
                throw new IllegalArgumentException("Unidade de medida invalida: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case UNIDADES:
                return "unidades";
            case DUZIAS:
                return "duzias";
            case KGS:
                return "kgs";
            case LITROS:
                return "litros";
            default:
                throw new IllegalStateException("Unidade de medida nao mapeada: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP (${p.unidade.dbValue}). */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case UNIDADES:
                return "Unidades";
            case DUZIAS:
                return "Dúzias";
            case KGS:
                return "Kgs";
            case LITROS:
                return "Litros";
            default:
                throw new IllegalStateException("Unidade de medida nao mapeada: " + this);
        }
    }
}
