package br.com.silsys.admsuporte.util;

import br.com.silsys.admsuporte.model.ChecklistPortaria;
import br.com.silsys.admsuporte.model.Unidade;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.OutputStream;
import java.util.List;

/**
 * Checklist em branco, em paisagem, no mesmo desenho da tela (checklistPortariaForm.jsp), para
 * preencher a mao. E uma tabela unica: cada linha tem um item a esquerda (OK, observacao e os
 * campos da validacao) e um grupo de unidades a direita (codigo + quantidade), todas as linhas
 * com a mesma altura (a do item mais comprido). O titulo, o topo (data/hora/usuario, validacao e
 * status) e os cabecalhos sao linhas de cabecalho da tabela: repetem em cada pagina.
 */
public final class ChecklistPdfGenerator {

    private static final float TAMANHO_FONTE = 12;
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, 14);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, "Cp1252", true, TAMANHO_FONTE);
    private static final Font TEXT_FONT = FontFactory.getFont(FontFactory.HELVETICA, "Cp1252", true, TAMANHO_FONTE);
    private static final Color CINZA = new Color(235, 235, 235);

    private static final float MARGEM = 18;
    private static final float LARGURA_UTIL = PageSize.A4.getHeight() - 2 * MARGEM; // paisagem
    private static final float PADDING = 3;
    private static final float ENTRELINHA = TAMANHO_FONTE * 1.2f;
    private static final float ALTURA_CAMPO = 22;

    /** Pares (unidade, quantidade) por linha, a direita. */
    private static final int UNIDADES_POR_LINHA = 5;
    private static final int COLUNAS_ITEM = 5;
    private static final int COLUNAS = COLUNAS_ITEM + 1 + UNIDADES_POR_LINHA * 2;

    /** Larguras em pontos: OK, item, observacao, valid., obs. validacao, espaco, e os pares das unidades. */
    private static final float[] LARGURAS_ITEM = {26, 200, 100, 46, 100};
    private static final float LARGURA_ESPACO = 10;
    private static final float LARGURA_CODIGO = 30;

    private ChecklistPdfGenerator() {
    }

    public static void generate(String nomeCondominio, List<ChecklistPortaria> itens, List<Unidade> unidades,
                                OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate(), MARGEM, MARGEM, 16, 16);
        PdfWriter.getInstance(document, out);
        document.open();

        float[] larguras = new float[COLUNAS];
        float usada = LARGURA_ESPACO;
        for (int i = 0; i < COLUNAS_ITEM; i++) {
            larguras[i] = LARGURAS_ITEM[i];
            usada += LARGURAS_ITEM[i];
        }
        larguras[COLUNAS_ITEM] = LARGURA_ESPACO;
        float larguraQtd = (LARGURA_UTIL - usada) / UNIDADES_POR_LINHA - LARGURA_CODIGO;
        for (int k = 0; k < UNIDADES_POR_LINHA; k++) {
            larguras[COLUNAS_ITEM + 1 + k * 2] = LARGURA_CODIGO;
            larguras[COLUNAS_ITEM + 2 + k * 2] = larguraQtd;
        }
        PdfPTable t = new PdfPTable(COLUNAS);
        t.setTotalWidth(larguras);
        t.setLockedWidth(true);

        // Altura unica das linhas de detalhe: a do item mais comprido (no minimo duas linhas de texto).
        int maxLinhas = 2;
        for (ChecklistPortaria item : itens) {
            maxLinhas = Math.max(maxLinhas, linhasNecessarias(item.getItemDescricao(), LARGURAS_ITEM[1] - 2 * PADDING));
        }
        float alturaLinha = maxLinhas * ENTRELINHA + 2 * PADDING + 4;

        // Cabecalho (repete em cada pagina): titulo, topo, e a linha de titulos das duas partes.
        String titulo = "Checklist";
        if (nomeCondominio != null && !nomeCondominio.trim().isEmpty()) {
            titulo += " - " + nomeCondominio.trim();
        }
        PdfPCell celTitulo = new PdfPCell(new Phrase(titulo, TITLE_FONT));
        celTitulo.setColspan(COLUNAS);
        celTitulo.setBorder(Rectangle.NO_BORDER);
        celTitulo.setPaddingBottom(6);
        t.addCell(celTitulo);

        PdfPCell celTopo = new PdfPCell(topo());
        celTopo.setColspan(COLUNAS);
        celTopo.setBorder(Rectangle.NO_BORDER);
        celTopo.setPadding(0);
        celTopo.setPaddingBottom(8);
        t.addCell(celTopo);

        cabecalho(t, "OK");
        cabecalho(t, "Item");
        cabecalho(t, "Observação");
        cabecalho(t, "Valid.");
        cabecalho(t, "Obs. validação");
        vazio(t, 1, ALTURA_CAMPO);
        PdfPCell celEncomendas = new PdfPCell(encomendas(larguraQtd));
        celEncomendas.setColspan(UNIDADES_POR_LINHA * 2);
        celEncomendas.setBorder(Rectangle.NO_BORDER);
        celEncomendas.setPadding(0);
        t.addCell(celEncomendas);
        t.setHeaderRows(3);

        // Detalhe: linha r = item r (se houver) + unidades r*N .. r*N+N-1 (se houver).
        int linhasUnidades = (unidades.size() + UNIDADES_POR_LINHA - 1) / UNIDADES_POR_LINHA;
        int linhas = Math.max(Math.max(itens.size(), linhasUnidades), 1);
        for (int r = 0; r < linhas; r++) {
            if (r < itens.size()) {
                caixa(t, alturaLinha);
                PdfPCell desc = new PdfPCell(new Phrase(itens.get(r).getItemDescricao(), TEXT_FONT));
                desc.setFixedHeight(alturaLinha);
                desc.setPadding(PADDING);
                desc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                t.addCell(desc);
                branco(t, alturaLinha);
                caixa(t, alturaLinha);
                branco(t, alturaLinha);
            } else if (r == 0 && itens.isEmpty()) {
                PdfPCell semItens = new PdfPCell(new Phrase("Nenhum item de checklist para o seu tipo de usuário.", TEXT_FONT));
                semItens.setColspan(COLUNAS_ITEM);
                semItens.setFixedHeight(alturaLinha);
                t.addCell(semItens);
            } else {
                vazio(t, COLUNAS_ITEM, alturaLinha);
            }
            vazio(t, 1, alturaLinha);
            for (int k = 0; k < UNIDADES_POR_LINHA; k++) {
                int idx = r * UNIDADES_POR_LINHA + k;
                if (idx < unidades.size()) {
                    PdfPCell cod = new PdfPCell(new Phrase(unidades.get(idx).getCodigo(), TEXT_FONT));
                    cod.setBackgroundColor(CINZA);
                    cod.setFixedHeight(alturaLinha);
                    cod.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cod.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    t.addCell(cod);
                    branco(t, alturaLinha);
                } else {
                    vazio(t, 2, alturaLinha);
                }
            }
        }
        document.add(t);
        document.close();
    }

    /** Quantas linhas o texto ocupa na largura dada (quebra por palavra, como o PDF faz). */
    private static int linhasNecessarias(String texto, float largura) {
        if (texto == null || texto.trim().isEmpty()) {
            return 1;
        }
        BaseFont bf = TEXT_FONT.getCalculatedBaseFont(false);
        float espaco = bf.getWidthPoint(" ", TAMANHO_FONTE);
        int linhas = 1;
        float atual = 0;
        for (String palavra : texto.trim().split("\\s+")) {
            float w = bf.getWidthPoint(palavra, TAMANHO_FONTE);
            if (atual > 0 && atual + espaco + w > largura) {
                linhas++;
                atual = w;
            } else {
                atual += (atual > 0 ? espaco : 0) + w;
            }
        }
        return linhas;
    }

    /**
     * Duas linhas: Data, Hora, Usuario, Validado por, Em | Status (opcoes para marcar), Obs. geral.
     * Larguras em pontos, somando a largura util da pagina.
     */
    private static PdfPTable topo() throws DocumentException {
        float[] w = {50, 95, 42, 60, 62, 150, 92, 130, 30, 0};
        float soma = 0;
        for (int i = 0; i < w.length - 1; i++) {
            soma += w[i];
        }
        w[w.length - 1] = LARGURA_UTIL - soma;
        PdfPTable t = new PdfPTable(w.length);
        t.setTotalWidth(w);
        t.setLockedWidth(true);

        rotulo(t, "Data");
        campo(t, 1);
        rotulo(t, "Hora");
        campo(t, 1);
        rotulo(t, "Usuário");
        campo(t, 1);
        rotulo(t, "Validado por");
        campo(t, 1);
        rotulo(t, "Em");
        campo(t, 1);

        rotulo(t, "Status");
        PdfPCell status = new PdfPCell(new Phrase(
                "( ) Em andamento    ( ) Disponível p/ validação    ( ) Validada", TEXT_FONT));
        status.setColspan(5);
        status.setMinimumHeight(ALTURA_CAMPO);
        status.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(status);
        rotulo(t, "Obs. geral");
        campo(t, 3);
        return t;
    }

    /** "Encomendas - s/baixa no app [ ] no estoque [ ]", na largura das colunas das unidades. */
    private static PdfPTable encomendas(float larguraQtd) throws DocumentException {
        float total = (LARGURA_CODIGO + larguraQtd) * UNIDADES_POR_LINHA;
        float[] w = {84, 88, 34, 66, 34};
        float soma = 0;
        for (float x : w) {
            soma += x;
        }
        w[0] += total - soma; // sobra fica no titulo
        PdfPTable t = new PdfPTable(w.length);
        t.setTotalWidth(w);
        t.setLockedWidth(true);
        PdfPCell titulo = new PdfPCell(new Phrase("Encomendas -", LABEL_FONT));
        titulo.setBorder(Rectangle.NO_BORDER);
        titulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(titulo);
        PdfPCell app = new PdfPCell(new Phrase("s/baixa no app", TEXT_FONT));
        app.setBorder(Rectangle.NO_BORDER);
        app.setHorizontalAlignment(Element.ALIGN_RIGHT);
        app.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(app);
        campo(t, 1);
        PdfPCell est = new PdfPCell(new Phrase("no estoque", TEXT_FONT));
        est.setBorder(Rectangle.NO_BORDER);
        est.setHorizontalAlignment(Element.ALIGN_RIGHT);
        est.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(est);
        campo(t, 1);
        return t;
    }

    private static void rotulo(PdfPTable t, String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, LABEL_FONT));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPaddingRight(4);
        t.addCell(c);
    }

    private static void campo(PdfPTable t, int colspan) {
        PdfPCell c = new PdfPCell(new Phrase(" ", TEXT_FONT));
        c.setColspan(colspan);
        c.setMinimumHeight(ALTURA_CAMPO);
        t.addCell(c);
    }

    private static void cabecalho(PdfPTable t, String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, LABEL_FONT));
        c.setBackgroundColor(CINZA);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setFixedHeight(ALTURA_CAMPO);
        c.setPadding(2);
        t.addCell(c);
    }

    private static void branco(PdfPTable t, float altura) {
        PdfPCell c = new PdfPCell(new Phrase(" ", TEXT_FONT));
        c.setFixedHeight(altura);
        t.addCell(c);
    }

    private static void vazio(PdfPTable t, int colspan, float altura) {
        PdfPCell c = new PdfPCell(new Phrase(" ", TEXT_FONT));
        c.setColspan(colspan);
        c.setBorder(Rectangle.NO_BORDER);
        c.setFixedHeight(altura);
        t.addCell(c);
    }

    /** Quadradinho para marcar a mao. */
    private static void caixa(PdfPTable t, float altura) {
        PdfPTable quadro = new PdfPTable(1);
        quadro.setTotalWidth(13);
        quadro.setLockedWidth(true);
        PdfPCell q = new PdfPCell(new Phrase(" ", TEXT_FONT));
        q.setFixedHeight(13);
        quadro.addCell(q);
        PdfPCell c = new PdfPCell(quadro);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setFixedHeight(altura);
        c.setPadding(PADDING);
        t.addCell(c);
    }
}
