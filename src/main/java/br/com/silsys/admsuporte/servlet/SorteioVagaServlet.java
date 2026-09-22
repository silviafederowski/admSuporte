package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.dao.VagaDao;
import br.com.silsys.admsuporte.model.SorteioVagaCell;
import br.com.silsys.admsuporte.model.Unidade;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sorteio de vagas: grade de unidades por andar (finais 1-4), mostrando a(s) vaga(s) atribuida(s)
 * a cada unidade no ano mais recente de histvagas, coloridas por tipo (interna/externa/
 * preferencial). O codigo da unidade codifica andar e final (ex.: "234" = 23 andar, final 4).
 * Acesso controlado pela tabela autorizacoes_menu, tela "sorteio-vagas".
 */
public class SorteioVagaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SorteioVagaServlet.class.getName());
    private static final int FINAIS = 4;

    private final VagaDao vagaDao = new VagaDao();
    private final UnidadeDao unidadeDao = new UnidadeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Integer ano = vagaDao.buscarAnoMaisRecente();
            request.setAttribute("ano", ano);
            if (ano != null) {
                List<VagaDao.PosicaoVaga> posicao = vagaDao.listPosicaoPorAno(ano);
                request.setAttribute("grade", montarGrade(posicao));

                request.setAttribute("anoSeguinte", ano + 1);
                Map<Integer, Map<Integer, Boolean>> mapaUnidadeAno = vagaDao.mapUnidadeAnoTodasInternas();
                request.setAttribute("gradePrevia", montarGradePrevia(posicao, mapaUnidadeAno, ano));
            }

            List<VagaDao.VagaSimples> vagasM2 = vagaDao.listVagasPorPavimento("-2");
            List<VagaDao.VagaSimples> vagasM1 = vagaDao.listVagasPorPavimento("-1");
            List<VagaDao.VagaSimples> vagas0 = vagaDao.listVagasPorPavimento("0");
            request.setAttribute("vagasAndarMenos2", montarListaPavimento(vagasM2));
            request.setAttribute("vagasAndarMenos1", montarListaPavimento(vagasM1));
            request.setAttribute("vagasAndar0", montarListaPavimento(vagas0));

            List<VagaDao.VagaSimples> todasVagas = new ArrayList<>();
            todasVagas.addAll(vagasM2);
            todasVagas.addAll(vagasM1);
            todasVagas.addAll(vagas0);
            List<String> vagasInternasIndividuais = new ArrayList<>();
            List<String> vagasInternasDuplas = new ArrayList<>();
            List<String> vagasExternasIndividuais = new ArrayList<>();
            List<String> vagasExternasDuplas = new ArrayList<>();
            for (VagaDao.VagaSimples v : todasVagas) {
                String especialUpper = v.especial == null ? "" : v.especial.toUpperCase();
                if (especialUpper.contains("IDOSO") || especialUpper.contains("PREFERENCIAL")
                        || especialUpper.contains("EMERG") || especialUpper.contains("ZELADOR")) {
                    continue;
                }
                boolean dupla = v.quantasVagas != null && v.quantasVagas >= 2;
                if ("I".equalsIgnoreCase(v.internaExterna)) {
                    (dupla ? vagasInternasDuplas : vagasInternasIndividuais).add(v.codigo);
                } else if ("E".equalsIgnoreCase(v.internaExterna)) {
                    (dupla ? vagasExternasDuplas : vagasExternasIndividuais).add(v.codigo);
                }
            }
            Collections.shuffle(vagasInternasIndividuais);
            Collections.shuffle(vagasInternasDuplas);
            Collections.shuffle(vagasExternasIndividuais);
            Collections.shuffle(vagasExternasDuplas);
            request.setAttribute("vagasInternasIndividuais", vagasInternasIndividuais);
            request.setAttribute("vagasInternasDuplas", vagasInternasDuplas);
            request.setAttribute("vagasExternasIndividuais", vagasExternasIndividuais);
            request.setAttribute("vagasExternasDuplas", vagasExternasDuplas);
            request.setAttribute("todasUnidades", unidadeDao.listAll());

            List<Unidade> candidatasIdoso = unidadeDao.listCandidatasIdoso();
            Collections.shuffle(candidatasIdoso);
            request.setAttribute("candidatasIdoso", candidatasIdoso);
            request.setAttribute("vagasIdoso", vagaDao.listCodigosVagaIdoso());
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar o sorteio de vagas: "
                    + ErrorMessages.friendly(LOGGER, "sorteio-vagas", "Montar grade do sorteio de vagas", e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/sorteioVagaList.jsp");
        dispatcher.forward(request, response);
    }

    private Map<Integer, List<SorteioVagaCell>> montarGrade(List<VagaDao.PosicaoVaga> linhas) {
        Map<String, List<VagaDao.PosicaoVaga>> porUnidade = new LinkedHashMap<>();
        for (VagaDao.PosicaoVaga linha : linhas) {
            porUnidade.computeIfAbsent(linha.unidadeCodigo, k -> new ArrayList<>()).add(linha);
        }

        Map<Integer, List<SorteioVagaCell>> grade = new TreeMap<>();
        for (Map.Entry<String, List<VagaDao.PosicaoVaga>> entry : porUnidade.entrySet()) {
            Integer codigoNumerico = paraInteiro(entry.getKey());
            if (codigoNumerico == null) {
                continue;
            }
            int andar = codigoNumerico / 10;
            int finalUnidade = codigoNumerico % 10;
            if (finalUnidade < 1 || finalUnidade > FINAIS) {
                continue;
            }
            List<SorteioVagaCell> linhaAndar = grade.computeIfAbsent(andar, k -> criarLinhaVazia());
            linhaAndar.set(finalUnidade - 1, criarCelula(entry.getValue(), entry.getKey()));
        }
        return grade;
    }

    /**
     * Previa do ano seguinte: uma celula por unidade (mostrando o codigo da propria unidade, nao
     * da vaga), pintada pelo criterio invertido: se a unidade esteve interna nos ultimos 3 anos
     * (incluindo o ano atual), fica externa na previa; caso contrario, fica interna. Nao
     * distingue individual/dupla nem preferencial.
     */
    private Map<Integer, List<SorteioVagaCell>> montarGradePrevia(
            List<VagaDao.PosicaoVaga> linhas, Map<Integer, Map<Integer, Boolean>> mapaUnidadeAno, int ano) {
        Map<String, VagaDao.PosicaoVaga> primeiraLinhaPorUnidade = new LinkedHashMap<>();
        for (VagaDao.PosicaoVaga linha : linhas) {
            primeiraLinhaPorUnidade.putIfAbsent(linha.unidadeCodigo, linha);
        }

        Map<Integer, List<SorteioVagaCell>> grade = new TreeMap<>();
        for (VagaDao.PosicaoVaga linha : primeiraLinhaPorUnidade.values()) {
            Integer codigoNumerico = paraInteiro(linha.unidadeCodigo);
            if (codigoNumerico == null) {
                continue;
            }
            int andar = codigoNumerico / 10;
            int finalUnidade = codigoNumerico % 10;
            if (finalUnidade < 1 || finalUnidade > FINAIS) {
                continue;
            }
            Map<Integer, Boolean> anosDaUnidade = mapaUnidadeAno.get(linha.unidadeChave);
            boolean tresAnosInterna = anosDaUnidade != null
                    && Boolean.TRUE.equals(anosDaUnidade.get(ano))
                    && Boolean.TRUE.equals(anosDaUnidade.get(ano - 1))
                    && Boolean.TRUE.equals(anosDaUnidade.get(ano - 2));
            String cssClass = tresAnosInterna ? "sorteio-externa" : "sorteio-interna";

            List<SorteioVagaCell> linhaAndar = grade.computeIfAbsent(andar, k -> criarLinhaVazia());
            linhaAndar.set(finalUnidade - 1, new SorteioVagaCell(linha.unidadeCodigo, cssClass, linha.unidadeCodigo));
        }
        return grade;
    }

    private List<SorteioVagaCell> criarLinhaVazia() {
        List<SorteioVagaCell> linha = new ArrayList<>();
        for (int i = 0; i < FINAIS; i++) {
            linha.add(new SorteioVagaCell("", ""));
        }
        return linha;
    }

    private SorteioVagaCell criarCelula(List<VagaDao.PosicaoVaga> vagasDaUnidade, String unidadeCodigo) {
        StringBuilder codigos = new StringBuilder();
        boolean temPreferencial = false;
        boolean todasInternas = true;
        boolean todasExternas = true;
        for (VagaDao.PosicaoVaga v : vagasDaUnidade) {
            if (codigos.length() > 0) {
                codigos.append("/");
            }
            codigos.append(v.vagaCodigo);
            if (v.especial != null && (v.especial.toUpperCase().contains("IDOSO")
                    || v.especial.toUpperCase().contains("PREFERENCIAL"))) {
                temPreferencial = true;
            }
            if (!"I".equalsIgnoreCase(v.internaExterna)) {
                todasInternas = false;
            }
            if (!"E".equalsIgnoreCase(v.internaExterna)) {
                todasExternas = false;
            }
        }
        String cssClass;
        if (temPreferencial) {
            cssClass = "sorteio-preferencial";
        } else {
            String base = todasInternas ? "sorteio-interna" : todasExternas ? "sorteio-externa" : "";
            boolean individual = vagasDaUnidade.size() <= 1;
            cssClass = individual ? (base + " sorteio-borda-individual").trim() : base;
        }
        return new SorteioVagaCell(codigos.toString(), cssClass, unidadeCodigo);
    }

    /** Vagas de um pavimento (garagem), na ordem do codigo, coloridas pela legenda completa. */
    private List<SorteioVagaCell> montarListaPavimento(List<VagaDao.VagaSimples> vagas) {
        List<SorteioVagaCell> result = new ArrayList<>();
        for (VagaDao.VagaSimples v : vagas) {
            result.add(new SorteioVagaCell(v.codigo, categoriaCss(v.internaExterna, v.quantasVagas, v.especial)));
        }
        return result;
    }

    /**
     * Categoria (classe CSS) de uma vaga, conforme a legenda "VAGAS POR TIPO": preferencial
     * (idosos) e vermelha sem borda; senao, cor base pela interna(azul)/externa(amarelo), com
     * borda preta se individual (quantasVagas &lt; 2) e sem borda se nao (dupla).
     */
    private String categoriaCss(String internaExterna, Integer quantasVagas, String especial) {
        String especialUpper = especial == null ? "" : especial.toUpperCase();
        if (especialUpper.contains("IDOSO") || especialUpper.contains("PREFERENCIAL")) {
            return "sorteio-preferencial";
        }
        String base;
        if (especialUpper.contains("EMERG")) {
            base = "sorteio-emergencia";
        } else if (especialUpper.contains("ZELADOR")) {
            base = "sorteio-zelador";
        } else if ("I".equalsIgnoreCase(internaExterna)) {
            base = "sorteio-interna";
        } else if ("E".equalsIgnoreCase(internaExterna)) {
            base = "sorteio-externa";
        } else {
            base = "";
        }
        boolean individual = quantasVagas == null || quantasVagas < 2;
        return individual ? (base + " sorteio-borda-individual").trim() : base;
    }

    private Integer paraInteiro(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
