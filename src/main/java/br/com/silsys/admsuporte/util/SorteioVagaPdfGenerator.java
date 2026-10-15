package br.com.silsys.admsuporte.util;

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
 * Monta o PDF com a transcricao das linhas exibidas na coluna "Sorteio" (3a coluna) da tela de
 * Sorteio de vagas: uma linha em negrito por titulo de fase ("Fase N — ..."), as demais em fonte
 * normal, na mesma ordem em que foram exibidas.
 */
public final class SorteioVagaPdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 14);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 11);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 10);

    private SorteioVagaPdfGenerator() {
    }

    public static void generate(List<String> linhas, OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Sorteio de vagas", TITLE_FONT));
        document.add(new Paragraph(" ", TEXT_FONT));

        for (String linha : linhas) {
            if (linha == null || linha.trim().isEmpty()) {
                continue;
            }
            Font font = linha.startsWith("Fase ") ? HEADER_FONT : TEXT_FONT;
            document.add(new Paragraph(linha, font));
        }

        document.close();
    }
}
