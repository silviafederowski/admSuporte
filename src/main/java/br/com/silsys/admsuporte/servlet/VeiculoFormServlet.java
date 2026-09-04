package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.CorDao;
import br.com.silsys.admsuporte.dao.MarcaDao;
import br.com.silsys.admsuporte.dao.ModeloDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.dao.VagaDao;
import br.com.silsys.admsuporte.dao.VeiculoDao;
import br.com.silsys.admsuporte.model.Unidade;
import br.com.silsys.admsuporte.model.Veiculo;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Formulario com ate 5 linhas de veiculos (marca/modelo/placas) para uma unidade.
 * Acesso liberado a qualquer usuario logado (ver AuthFilter) - qualquer condomino pode
 * editar os veiculos da sua unidade, sem restricao de nivel.
 */
public class VeiculoFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VeiculoFormServlet.class.getName());
    private static final int MAX_LINHAS = 5;

    private final UnidadeDao unidadeDao = new UnidadeDao();
    private final VagaDao vagaDao = new VagaDao();
    private final VeiculoDao veiculoDao = new VeiculoDao();
    private final MarcaDao marcaDao = new MarcaDao();
    private final ModeloDao modeloDao = new ModeloDao();
    private final CorDao corDao = new CorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer unidadeChave = parseInt(request.getParameter("unidade"));
        if (unidadeChave == null) {
            response.sendRedirect(request.getContextPath() + "/veiculos");
            return;
        }
        try {
            Unidade unidade = unidadeDao.findByChave(unidadeChave);
            if (unidade == null) {
                response.sendRedirect(request.getContextPath() + "/veiculos");
                return;
            }
            request.setAttribute("unidade", unidade);
            request.setAttribute("veiculos", padToFive(veiculoDao.listByUnidade(unidadeChave)));
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "veiculos", "Carregar veículos da unidade", e);
            response.sendRedirect(request.getContextPath() + "/veiculos");
            return;
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer unidadeChave = parseInt(request.getParameter("unidadeChave"));
        if (unidadeChave == null) {
            response.sendRedirect(request.getContextPath() + "/veiculos");
            return;
        }

        Unidade unidade;
        try {
            unidade = unidadeDao.findByChave(unidadeChave);
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "veiculos", "Carregar unidade", e);
            response.sendRedirect(request.getContextPath() + "/veiculos");
            return;
        }
        if (unidade == null) {
            response.sendRedirect(request.getContextPath() + "/veiculos");
            return;
        }

        Map<String, String> errors = new HashMap<>();
        List<Veiculo> linhas = new ArrayList<>();
        List<Veiculo> paraSalvar = new ArrayList<>();

        for (int i = 1; i <= MAX_LINHAS; i++) {
            String corIdParam = request.getParameter("corId" + i);
            String marcaIdParam = request.getParameter("marcaId" + i);
            String modeloIdParam = request.getParameter("modeloId" + i);
            String placas = request.getParameter("placas" + i);

            Veiculo linha = new Veiculo();
            Integer corId = parseInt(corIdParam);
            Integer marcaId = parseInt(marcaIdParam);
            Integer modeloId = parseInt(modeloIdParam);
            if (corId != null) {
                linha.setCorId(corId);
            }
            if (marcaId != null) {
                linha.setMarcaId(marcaId);
            }
            if (modeloId != null) {
                linha.setModeloId(modeloId);
            }
            linha.setPlacas(placas);
            linhas.add(linha);

            boolean algumPreenchido = corId != null || marcaId != null || modeloId != null || !ValidationUtil.isBlank(placas);
            if (!algumPreenchido) {
                continue;
            }
            if (corId == null || marcaId == null || modeloId == null || ValidationUtil.isBlank(placas)) {
                errors.put("linha" + i, "Preencha cor, marca, modelo e placas, ou deixe a linha em branco.");
                continue;
            }
            paraSalvar.add(linha);
        }

        if (errors.isEmpty()) {
            try {
                veiculoDao.salvarPorUnidade(unidadeChave, paraSalvar);
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Veículos",
                        "Atualizar veículos da unidade: " + unidade.getCodigo());
                response.sendRedirect(request.getContextPath() + "/veiculos?atualizado=1");
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar os veículos: "
                        + ErrorMessages.friendly(LOGGER, "veiculos", "Salvar veículos", e));
            }
        }

        request.setAttribute("unidade", unidade);
        request.setAttribute("veiculos", linhas);
        request.setAttribute("errors", errors);
        forward(request, response);
    }

    private List<Veiculo> padToFive(List<Veiculo> veiculos) {
        List<Veiculo> result = new ArrayList<>(veiculos);
        while (result.size() < MAX_LINHAS) {
            result.add(new Veiculo());
        }
        return result;
    }

    private Integer parseInt(String value) {
        if (ValidationUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("cores", corDao.listAll());
            request.setAttribute("marcas", marcaDao.listAll());
            request.setAttribute("modelos", modeloDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "veiculos", "Carregar cores/marcas/modelos para formulário", e);
            request.setAttribute("cores", java.util.Collections.emptyList());
            request.setAttribute("marcas", java.util.Collections.emptyList());
            request.setAttribute("modelos", java.util.Collections.emptyList());
        }

        Object unidadeAttr = request.getAttribute("unidade");
        if (unidadeAttr instanceof Unidade) {
            try {
                List<String> codigosVaga = vagaDao.listCodigosVagaAtual(((Unidade) unidadeAttr).getChave());
                request.setAttribute("codigosVagaUnidade", String.join(", ", codigosVaga));
            } catch (SQLException e) {
                ErrorMessages.logErro(LOGGER, "veiculos", "Carregar vagas da unidade", e);
                request.setAttribute("codigosVagaUnidade", "");
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/veiculoForm.jsp");
        dispatcher.forward(request, response);
    }
}
