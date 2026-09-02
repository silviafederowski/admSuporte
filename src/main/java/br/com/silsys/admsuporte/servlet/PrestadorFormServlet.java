package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.PrestadorDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Classificacao;
import br.com.silsys.admsuporte.model.Prestador;
import br.com.silsys.admsuporte.model.TipoServico;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Formulario de criacao/edicao de prestador. Acesso restrito a administrador/zelador. */
public class PrestadorFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PrestadorFormServlet.class.getName());

    private final PrestadorDao prestadorDao = new PrestadorDao();
    private final ServicoDao servicoDao = new ServicoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        Prestador prestador = new Prestador();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                prestador = prestadorDao.findById(Integer.parseInt(idParam));
                if (prestador == null) {
                    response.sendRedirect(request.getContextPath() + "/prestadores");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao carregar prestador para edicao.", e);
                response.sendRedirect(request.getContextPath() + "/prestadores");
                return;
            }
        }
        request.setAttribute("prestador", prestador);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String nomeRazaoSocial = request.getParameter("nomeRazaoSocial");
        String email = request.getParameter("email");
        String contato1Nome = request.getParameter("contato1Nome");
        String contato1Cargo = request.getParameter("contato1Cargo");
        String contato1Telefone = request.getParameter("contato1Telefone");
        String contato2Nome = request.getParameter("contato2Nome");
        String contato2Cargo = request.getParameter("contato2Cargo");
        String contato2Telefone = request.getParameter("contato2Telefone");
        String contato3Nome = request.getParameter("contato3Nome");
        String contato3Cargo = request.getParameter("contato3Cargo");
        String contato3Telefone = request.getParameter("contato3Telefone");
        String classificacaoParam = request.getParameter("classificacao");
        String tipoParam = request.getParameter("tipo");
        String observacao = request.getParameter("observacao");
        String[] servicoIdParams = request.getParameterValues("servicoIds");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(nomeRazaoSocial)) {
            errors.put("nomeRazaoSocial", "Informe o nome ou razão social.");
        }
        Classificacao classificacao = null;
        if (ValidationUtil.isBlank(classificacaoParam)) {
            errors.put("classificacao", "Selecione a classificação.");
        } else {
            try {
                classificacao = Classificacao.fromDbValue(classificacaoParam);
            } catch (IllegalArgumentException e) {
                errors.put("classificacao", "Classificação inválida.");
            }
        }
        TipoServico tipo = null;
        if (ValidationUtil.isBlank(tipoParam)) {
            errors.put("tipo", "Selecione o tipo.");
        } else {
            try {
                tipo = TipoServico.fromDbValue(tipoParam);
            } catch (IllegalArgumentException e) {
                errors.put("tipo", "Valor inválido.");
            }
        }

        Prestador prestador = new Prestador();
        prestador.setNomeRazaoSocial(nomeRazaoSocial);
        prestador.setEmail(email);
        prestador.setContato1Nome(contato1Nome);
        prestador.setContato1Cargo(contato1Cargo);
        prestador.setContato1Telefone(contato1Telefone);
        prestador.setContato2Nome(contato2Nome);
        prestador.setContato2Cargo(contato2Cargo);
        prestador.setContato2Telefone(contato2Telefone);
        prestador.setContato3Nome(contato3Nome);
        prestador.setContato3Cargo(contato3Cargo);
        prestador.setContato3Telefone(contato3Telefone);
        prestador.setClassificacao(classificacao);
        prestador.setTipo(tipo);
        prestador.setObservacao(observacao);
        prestador.setServicoIds(parseServicoIds(servicoIdParams));

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        Integer id = null;
        if (isEdit) {
            try {
                id = Integer.parseInt(idParam);
                prestador.setId(id);
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    prestadorDao.update(prestador);
                    operacaoLogDao.registrar(usuarioLogado, "Prestadores",
                            "Editar prestador: " + prestador.getNomeRazaoSocial());
                    response.sendRedirect(request.getContextPath() + "/prestadores?atualizado=1");
                } else {
                    prestadorDao.create(prestador);
                    operacaoLogDao.registrar(usuarioLogado, "Prestadores",
                            "Criar prestador: " + prestador.getNomeRazaoSocial());
                    response.sendRedirect(request.getContextPath() + "/prestadores?criado=1");
                }
                return;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao salvar prestador.", e);
                errors.put("form", "Não foi possível salvar o prestador: " + ErrorMessages.describe(e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("prestador", prestador);
        forward(request, response);
    }

    private List<Integer> parseServicoIds(String[] servicoIdParams) {
        List<Integer> ids = new ArrayList<>();
        if (servicoIdParams == null) {
            return ids;
        }
        for (String value : servicoIdParams) {
            try {
                ids.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Ignorando id de servico invalido: " + value, e);
            }
        }
        return ids;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("servicos", servicoDao.listAll());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar servicos.", e);
            request.setAttribute("servicos", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/prestadorForm.jsp");
        dispatcher.forward(request, response);
    }
}
