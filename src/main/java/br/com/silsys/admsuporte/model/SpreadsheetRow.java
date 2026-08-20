package br.com.silsys.admsuporte.model;

import java.io.Serializable;

/**
 * Uma linha da planilha "Form" ja processada: rotulo (coluna D - coluna E,
 * como no app mobile) e os valores das colunas A..T usados para montar o PDF.
 */
public class SpreadsheetRow implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int COLUMN_COUNT = 20; // colunas A (0) a T (19)

    private final String label;
    private final String[] values;

    public SpreadsheetRow(String label, String[] values) {
        this.label = label;
        this.values = values;
    }

    public String getLabel() {
        return label;
    }

    public String getValue(int columnIndex) {
        return columnIndex >= 0 && columnIndex < values.length ? values[columnIndex] : "";
    }
}
