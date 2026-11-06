package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.Agenda;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Monta o PDF da lista da agenda (os compromissos que estavam visiveis com os filtros da tela).
 * Ordem "data": cronologica (data e hora; sem data por ultimo). Ordem "pontoFocal": agrupado por
 * ponto focal em ordem alfabetica e, dentro de cada um, cronologica.
 */
public final class AgendaPdfGenerator {

    public static final String ORDEM_DATA = "data";
    public static final String ORDEM_PONTO_FOCAL = "pontoFocal";

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 14);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 9);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 9);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, 9);
    private static final Font GRUPO_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 10);

    private static final Comparator<Agenda> CRONOLOGICA = Comparator
            .comparing(Agenda::getData, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))
            .thenComparing(Agenda::getHora, Comparator.nullsLast(Comparator.<LocalTime>naturalOrder()))
            .thenComparingInt(Agenda::getId);

    private AgendaPdfGenerator() {
    }

    public static void generate(String nomeCondominio, List<Agenda> compromissos, String ordem, OutputStream out)
            throws DocumentException {
        boolean porPontoFocal = ORDEM_PONTO_FOCAL.equals(ordem);

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();

        String titulo = "Agenda";
        if (nomeCondominio != null && !nomeCondominio.trim().isEmpty()) {
            titulo += " - " + nomeCondominio.trim();
        }
        document.add(new Paragraph(titulo, TITLE_FONT));
        document.add(new Paragraph("Ordenado por " + (porPontoFocal ? "ponto focal" : "data")
                + " - " + compromissos.size() + " compromisso(s)", SUBTITLE_FONT));
        document.add(new Paragraph(" ", TEXT_FONT));

        if (compromissos.isEmpty()) {
            document.add(new Paragraph("Nenhum compromisso na lista filtrada.", TEXT_FONT));
            document.close();
            return;
        }

        List<Agenda> ordenados = new ArrayList<>(compromissos);
        if (porPontoFocal) {
            ordenados.sort(Comparator
                    .comparing((Agenda a) -> textoOuVazio(a.getPontoFocal()).isEmpty())
                    .thenComparing(a -> textoOuVazio(a.getPontoFocal()).toUpperCase())
                    .thenComparing(CRONOLOGICA));
        } else {
            ordenados.sort(CRONOLOGICA);
        }

        // Agrupado por ponto focal, a coluna sai da tabela e vira a linha de titulo do grupo.
        float[] larguras = porPontoFocal
                ? new float[]{0.8f, 0.6f, 0.8f, 2.5f, 1.7f, 1.1f, 3f}
                : new float[]{0.8f, 0.6f, 1.2f, 0.8f, 2.2f, 1.6f, 1.1f, 2.8f};
        PdfPTable table = new PdfPTable(larguras.length);
        table.setWidthPercentage(100);
        table.setWidths(larguras);
        table.setHeaderRows(1);

        addHeaderCell(table, "Data");
        addHeaderCell(table, "Hora");
        if (!porPontoFocal) {
            addHeaderCell(table, "Ponto focal");
        }
        addHeaderCell(table, "Prioridade");
        addHeaderCell(table, "Assunto");
        addHeaderCell(table, "Serviço / Prestador");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Observação");

        String pontoFocalAtual = null;
        boolean primeiroGrupo = true;
        for (Agenda a : ordenados) {
            String pontoFocal = textoOuVazio(a.getPontoFocal());
            if (porPontoFocal && (primeiroGrupo || !Objects.equals(pontoFocal, pontoFocalAtual))) {
                addGrupoRow(table, larguras.length, pontoFocal.isEmpty() ? "Sem ponto focal" : pontoFocal);
                pontoFocalAtual = pontoFocal;
                primeiroGrupo = false;
            }

            addCell(table, a.getDataFormatada());
            addCell(table, a.getHoraFormatada());
            if (!porPontoFocal) {
                addCell(table, pontoFocal);
            }
            addCell(table, a.getPrioridade() != null ? a.getPrioridade().getLabel() : null);
            addCell(table, a.getAssunto());
            String servico = textoOuVazio(a.getServicoDescricao());
            if (!servico.isEmpty() && !textoOuVazio(a.getPrestadorNome()).isEmpty()) {
                servico += "\n" + a.getPrestadorNome();
            }
            addCell(table, servico);
            String status = a.getStatus().getLabel();
            if (a.getDataStatus() != null) {
                status += "\n" + a.getDataStatusFormatada();
            }
            addCell(table, status);
            addCell(table, a.getObservacao());
        }

        document.add(table);
        document.close();
    }

    private static String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
        cell.setPadding(4);
        cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text) {
        String valor = textoOuVazio(text);
        PdfPCell cell = new PdfPCell(new Paragraph(valor.isEmpty() ? "-" : valor, TEXT_FONT));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private static void addGrupoRow(PdfPTable table, int colspan, String pontoFocal) {
        PdfPCell cell = new PdfPCell(new Paragraph("Ponto focal: " + pontoFocal, GRUPO_FONT));
        cell.setColspan(colspan);
        cell.setPadding(5);
        cell.setPaddingTop(8);
        cell.setBackgroundColor(new java.awt.Color(245, 245, 245));
        table.addCell(cell);
    }
}
