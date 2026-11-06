package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.Veiculo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Monta o PDF da tela de Veiculos com as mesmas colunas e na mesma ordem das linhas exibidas.
 * Como na tela, unidade, vagas e observacao so aparecem na primeira linha de cada unidade.
 */
public final class VeiculoPdfGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 14);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 9);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 9);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 9);

    private VeiculoPdfGenerator() {
    }

    public static void generate(String nomeCondominio, List<Veiculo> veiculos, OutputStream out)
            throws DocumentException {
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();

        String titulo = "Veículos";
        if (nomeCondominio != null && !nomeCondominio.trim().isEmpty()) {
            titulo += " - " + nomeCondominio.trim();
        }
        document.add(new Paragraph(titulo, TITLE_FONT));
        document.add(new Paragraph(veiculos.size() + " linha(s)", SUBTITLE_FONT));
        document.add(new Paragraph(" ", TEXT_FONT));

        if (veiculos.isEmpty()) {
            document.add(new Paragraph("Nenhuma linha na lista filtrada.", TEXT_FONT));
            document.close();
            return;
        }

        // Mesmas proporcoes das colunas da tela (veiculoList.jsp).
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.7f, 0.9f, 1.1f, 1.1f, 0.9f, 1.1f, 4.2f});
        table.setHeaderRows(1);

        addHeaderCell(table, "Unid");
        addHeaderCell(table, "Vagas");
        addHeaderCell(table, "Marca");
        addHeaderCell(table, "Modelo");
        addHeaderCell(table, "Cor");
        addHeaderCell(table, "Placas");
        addHeaderCell(table, "Observação");

        String unidadeAnterior = null;
        for (Veiculo v : veiculos) {
            boolean primeiraDaUnidade = !Objects.equals(v.getUnidadeCodigo(), unidadeAnterior);
            addCell(table, primeiraDaUnidade ? v.getUnidadeCodigo() : null);
            addCell(table, primeiraDaUnidade ? v.getVagaAtual() : null);
            addCell(table, v.getMarcaDescricao());
            addCell(table, v.getModeloDescricao());
            addCell(table, v.getCorDescricao());
            addCell(table, v.getPlacas());
            addCell(table, primeiraDaUnidade ? v.getVagaObservacao() : null);
            unidadeAnterior = v.getUnidadeCodigo();
        }

        document.add(table);
        document.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
        cell.setPadding(4);
        cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text == null ? "" : text.trim(), TEXT_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
