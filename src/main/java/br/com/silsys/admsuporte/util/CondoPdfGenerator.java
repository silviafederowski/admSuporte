package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.SpreadsheetRow;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.OutputStream;
import java.util.List;

/**
 * Monta o PDF com os dados de cada unidade selecionada, equivalente a
 * buildPdfHtml() de GoogleFormScreen.tsx no app mobile: uma pagina por linha
 * selecionada, com secoes de contato, ambulancia, hospital, convenio, pets e
 * outras informacoes.
 */
public final class CondoPdfGenerator {

    private static final int COLUMN_A = 0;
    private static final int COLUMN_B = 1;
    private static final int COLUMN_C = 2;
    private static final int COLUMN_D = 3;
    private static final int COLUMN_E = 4;
    private static final int COLUMN_F = 5;
    private static final int COLUMN_G = 6;
    private static final int COLUMN_H = 7;
    private static final int COLUMN_I = 8;
    private static final int COLUMN_J = 9;
    private static final int COLUMN_K = 10;
    private static final int COLUMN_L = 11;
    private static final int COLUMN_M = 12;
    private static final int COLUMN_N = 13;
    private static final int COLUMN_O = 14;
    private static final int COLUMN_P = 15;
    private static final int COLUMN_Q = 16;
    private static final int COLUMN_R = 17;
    private static final int COLUMN_S = 18;
    private static final int COLUMN_T = 19;

    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 12);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 10);

    private CondoPdfGenerator() {
    }

    public static void generate(List<SpreadsheetRow> rows, OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        boolean first = true;
        for (SpreadsheetRow row : rows) {
            if (!first) {
                document.newPage();
            }
            first = false;
            addUnitPage(document, row);
        }

        document.close();
    }

    private static void addUnitPage(Document document, SpreadsheetRow row) throws DocumentException {
        header(document, "Unidade: " + row.getValue(COLUMN_D) + "          " + row.getValue(COLUMN_E).toUpperCase());
        blank(document);

        text(document, "Telefone: " + row.getValue(COLUMN_F) + "          " + row.getValue(COLUMN_C)
                + "          Deseja Grupo Avisos? " + row.getValue(COLUMN_G));
        text(document, "Email: " + row.getValue(COLUMN_B));
        blank(document);

        header(document, "CONTATOS");
        text(document, "Nomes dos contatos: " + row.getValue(COLUMN_H));
        text(document, "Tels dos contatos: " + row.getValue(COLUMN_I));
        text(document, "Contatos têm chave/senha? " + row.getValue(COLUMN_J));
        text(document, "Contatos Moradores? " + row.getValue(COLUMN_K));
        text(document, "Unidade dos contatos: " + row.getValue(COLUMN_L));
        blank(document);

        header(document, "AMBULÂNCIA");
        text(document, "Chamar SAMU? " + row.getValue(COLUMN_M));
        blank(document);

        header(document, "HOSPITAL");
        text(document, "Hospital a ser chamado: " + row.getValue(COLUMN_N));
        text(document, "Tel(s) do hospital: " + row.getValue(COLUMN_O));
        blank(document);

        header(document, "CONVÊNIO");
        text(document, "Convênio: " + row.getValue(COLUMN_P));
        text(document, "Carteirinha: " + row.getValue(COLUMN_Q));
        blank(document);

        header(document, "ANIMAIS DE ESTIMAÇÃO");
        text(document, "Animal(is) de estimação? " + row.getValue(COLUMN_R));
        text(document, "Sobre os animais: " + row.getValue(COLUMN_S));
        blank(document);

        header(document, "OUTRAS INFORMAÇÕES");
        text(document, row.getValue(COLUMN_T));
        blank(document);

        text(document, "Última atualização em " + row.getValue(COLUMN_A));
    }

    private static void header(Document document, String text) throws DocumentException {
        document.add(new Paragraph(text, HEADER_FONT));
    }

    private static void text(Document document, String text) throws DocumentException {
        document.add(new Paragraph(text, TEXT_FONT));
    }

    private static void blank(Document document) throws DocumentException {
        document.add(new Paragraph(" ", TEXT_FONT));
    }
}
