package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.SpreadsheetRow;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Le a planilha .xlsx/.xls enviada e monta as linhas ja ordenadas, equivalente
 * ao handlePick() de GoogleFormScreen.tsx no app mobile: colunas D e E viram o
 * rotulo, linhas com D e E vazias sao descartadas, e a ordenacao e numerica
 * quando os dois lados forem numeros, senao alfabetica (pt-BR, ignorando
 * maiusculas/minusculas e acentos).
 */
public final class SpreadsheetParser {

    private static final int COLUMN_A = 0;
    private static final int COLUMN_D = 3;
    private static final int COLUMN_E = 4;

    private static final Collator PT_BR_COLLATOR = Collator.getInstance(new Locale("pt", "BR"));
    static {
        PT_BR_COLLATOR.setStrength(Collator.PRIMARY);
    }

    private SpreadsheetParser() {
    }

    public static List<SpreadsheetRow> parseFormSheet(InputStream in) throws IOException, AppException {
        try (Workbook workbook = WorkbookFactory.create(in)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new AppException("A planilha enviada nao tem nenhuma aba com dados.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(new Locale("pt", "BR"));
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            List<SpreadsheetRow> rows = new ArrayList<>();
            int firstDataRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();

            for (int rowIndex = firstDataRow; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String dText = cellToText(row.getCell(COLUMN_D), formatter, evaluator);
                String eText = cellToText(row.getCell(COLUMN_E), formatter, evaluator);
                if (dText.isEmpty() && eText.isEmpty()) {
                    continue;
                }

                String[] values = new String[SpreadsheetRow.COLUMN_COUNT];
                for (int col = 0; col < SpreadsheetRow.COLUMN_COUNT; col++) {
                    Cell cell = row.getCell(col);
                    values[col] = col == COLUMN_A
                            ? cellToDateTimeText(cell, formatter, evaluator)
                            : cellToText(cell, formatter, evaluator);
                }

                rows.add(new SpreadsheetRow(dText + " - " + eText, values));
            }

            if (rows.isEmpty()) {
                throw new AppException("As colunas D e E da planilha enviada estao vazias.");
            }

            rows.sort((a, b) -> compareAscending(sortKey(a), sortKey(b)));
            return rows;
        }
    }

    private static String sortKey(SpreadsheetRow row) {
        return row.getValue(COLUMN_D);
    }

    private static int compareAscending(String a, String b) {
        Double numA = tryParseNumber(a);
        Double numB = tryParseNumber(b);
        if (!a.isEmpty() && !b.isEmpty() && numA != null && numB != null) {
            return Double.compare(numA, numB);
        }
        return PT_BR_COLLATOR.compare(a, b);
    }

    private static Double tryParseNumber(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String cellToText(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell, evaluator).trim();
    }

    private static String cellToDateTimeText(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDateTime dt = cell.getLocalDateTimeCellValue();
            return String.format("%02d/%02d/%04d %02d:%02d",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute());
        }
        return cellToText(cell, formatter, evaluator);
    }
}
