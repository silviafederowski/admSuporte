package br.com.silsys.admsuporte.model;

/** Unidade de tempo da periodicidade de um servico (coluna servicos.unidade_periodicidade). */
public enum PeriodicidadeUnidade {
    DIA,
    MES,
    ANO;

    public static PeriodicidadeUnidade fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.trim().toLowerCase()) {
            case "dia":
                return DIA;
            case "mes":
                return MES;
            case "ano":
                return ANO;
            default:
                throw new IllegalArgumentException("Unidade de periodicidade invalida: " + value);
        }
    }

    public String dbValue() {
        switch (this) {
            case DIA:
                return "dia";
            case MES:
                return "mes";
            case ANO:
                return "ano";
            default:
                throw new IllegalStateException("Unidade de periodicidade nao mapeada: " + this);
        }
    }

    /** Alias JavaBean de dbValue(), para uso em EL de JSP (${s.unidadePeriodicidade.dbValue}). */
    public String getDbValue() {
        return dbValue();
    }

    public String getLabel() {
        switch (this) {
            case DIA:
                return "dia(s)";
            case MES:
                return "mes(es)";
            case ANO:
                return "ano(s)";
            default:
                throw new IllegalStateException("Unidade de periodicidade nao mapeada: " + this);
        }
    }
}
