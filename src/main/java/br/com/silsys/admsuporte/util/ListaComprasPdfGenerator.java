package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.Produto;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Monta o PDF da lista de compras de um tipo de produto: todos os produtos daquele tipo com o
 * campo "comprar" preenchido, agrupados por fornecedor da ultima atualizacao (ordem alfabetica,
 * sem fornecedor por ultimo), em ordem alfabetica de descricao dentro de cada fornecedor, com
 * subtotal por fornecedor e total geral ao final.
 */
public final class ListaComprasPdfGenerator {

    private static final int COLUMN_COUNT = 5;

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 14);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 10);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 10);
    private static final Font FORNECEDOR_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 11);
    private static final Font SUBTOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 10);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 12);

    private ListaComprasPdfGenerator() {
    }

    public static void generate(String tipoDescricao, List<Produto> produtos, OutputStream out)
            throws DocumentException {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, out);
        document.open();

        document.add(new Paragraph("Lista de compras - " + tipoDescricao, TITLE_FONT));
        document.add(new Paragraph(" ", TEXT_FONT));

        if (produtos.isEmpty()) {
            document.add(new Paragraph("Nenhum produto deste tipo está marcado para compra.", TEXT_FONT));
            document.close();
            return;
        }

        List<Produto> ordenados = new ArrayList<>(produtos);
        ordenados.sort(Comparator
                .comparing((Produto p) -> p.getUltimoFornecedorNome() == null)
                .thenComparing(p -> p.getUltimoFornecedorNome() == null ? "" : p.getUltimoFornecedorNome().toUpperCase())
                .thenComparing(p -> p.getDescricao() == null ? "" : p.getDescricao().toUpperCase()));

        PdfPTable table = new PdfPTable(COLUMN_COUNT);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.5f, 1.3f, 1f, 1.3f, 1.3f});

        addHeaderCell(table, "Descrição");
        addHeaderCell(table, "Unidade");
        addHeaderCell(table, "Comprar");
        addHeaderCell(table, "Valor pago");
        addHeaderCell(table, "Valor total");

        BigDecimal totalGeral = BigDecimal.ZERO;
        BigDecimal subtotalFornecedor = BigDecimal.ZERO;
        String fornecedorAtual = null;
        boolean primeiroGrupo = true;

        for (Produto p : ordenados) {
            String fornecedor = p.getUltimoFornecedorNome();
            if (primeiroGrupo || !Objects.equals(fornecedor, fornecedorAtual)) {
                if (!primeiroGrupo) {
                    addSubtotalRow(table, subtotalFornecedor);
                }
                addFornecedorRow(table, fornecedor != null ? fornecedor : "Sem fornecedor");
                fornecedorAtual = fornecedor;
                subtotalFornecedor = BigDecimal.ZERO;
                primeiroGrupo = false;
            }

            BigDecimal quantidade = p.getComprar() != null ? p.getComprar() : BigDecimal.ZERO;
            BigDecimal valorPago = p.getValorUltimaCompra();

            addCell(table, p.getDescricao());
            addCell(table, p.getUnidade() != null ? p.getUnidade().getLabel() : "-");
            addCell(table, quantidade.setScale(2, RoundingMode.HALF_UP).toString());
            if (valorPago != null) {
                BigDecimal valorTotal = valorPago.multiply(quantidade)
                        .setScale(2, RoundingMode.HALF_UP);
                totalGeral = totalGeral.add(valorTotal);
                subtotalFornecedor = subtotalFornecedor.add(valorTotal);
                addCell(table, "R$ " + valorPago.setScale(2, RoundingMode.HALF_UP));
                addCell(table, "R$ " + valorTotal);
            } else {
                addCell(table, "-");
                addCell(table, "-");
            }
        }
        addSubtotalRow(table, subtotalFornecedor);

        document.add(table);
        document.add(new Paragraph(" ", TEXT_FONT));
        document.add(new Paragraph("Total geral: R$ " + totalGeral.setScale(2, RoundingMode.HALF_UP), TOTAL_FONT));

        document.close();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, TEXT_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void addFornecedorRow(PdfPTable table, String fornecedor) {
        PdfPCell cell = new PdfPCell(new Paragraph("Fornecedor: " + fornecedor, FORNECEDOR_FONT));
        cell.setColspan(COLUMN_COUNT);
        cell.setPadding(5);
        cell.setPaddingTop(10);
        cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
        table.addCell(cell);
    }

    private static void addSubtotalRow(PdfPTable table, BigDecimal subtotal) {
        PdfPCell cell = new PdfPCell(new Paragraph(
                "Subtotal: R$ " + subtotal.setScale(2, RoundingMode.HALF_UP), SUBTOTAL_FONT));
        cell.setColspan(COLUMN_COUNT);
        cell.setPadding(5);
        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        table.addCell(cell);
    }
}
