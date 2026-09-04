package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.SchemaInitializer;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.AutorizacaoMenu;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Formulario de criacao/edicao de uma autorizacao de menu. Regra fixa (nao depende da propria
 * tabela): qualquer usuario logado pode consultar (GET); so o sindico (administrador, nivel 0)
 * pode gravar (POST).
 */
public class AutorizacaoMenuFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AutorizacaoMenuFormServlet.class.getName());
    private static final int NIVEL_SINDICO = 0;

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        AutorizacaoMenu autorizacao = new AutorizacaoMenu();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                autorizacao = autorizacaoMenuDao.findById(Integer.parseInt(idParam));
                if (autorizacao == null) {
                    response.sendRedirect(request.getContextPath() + "/autorizacoes");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "autorizacoes", "Carregar autorização para edição", e);
                response.sendRedirect(request.getContextPath() + "/autorizacoes");
                return;
            }
        }
        request.setAttribute("autorizacao", autorizacao);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object nivelAttr = request.getSession().getAttribute("userNivel");
        int nivelLogado = nivelAttr instanceof Integer ? (Integer) nivelAttr : Integer.MAX_VALUE;
        if (nivelLogado != NIVEL_SINDICO) {
            response.sendRedirect(request.getContextPath() + "/autorizacoes");
            return;
        }

        String idParam = request.getParameter("id");
        String telaCodigo = request.getParameter("telaCodigo");
        String telaDescricao = request.getParameter("telaDescricao");
        String nivelParam = request.getParameter("nivel");
        String tipoAcesso = request.getParameter("tipoAcesso");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(telaCodigo)) {
            errors.put("telaCodigo", "Informe o código da tela.");
        }
        if (ValidationUtil.isBlank(telaDescricao)) {
            errors.put("telaDescricao", "Informe uma breve descrição.");
        }
        Integer nivel = null;
        if (ValidationUtil.isBlank(nivelParam)) {
            errors.put("nivel", "Selecione o nível.");
        } else {
            try {
                nivel = Integer.parseInt(nivelParam.trim());
            } catch (NumberFormatException e) {
                errors.put("nivel", "Nível inválido.");
            }
        }
        if (!"consulta".equals(tipoAcesso) && !"edicao".equals(tipoAcesso)) {
            errors.put("tipoAcesso", "Selecione o tipo de acesso.");
        }

        AutorizacaoMenu autorizacao = new AutorizacaoMenu();
        autorizacao.setTelaCodigo(telaCodigo);
        autorizacao.setTelaDescricao(telaDescricao);
        if (nivel != null) {
            autorizacao.setNivel(nivel);
        }
        autorizacao.setTipoAcesso(tipoAcesso);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        Integer id = null;
        if (isEdit) {
            try {
                id = Integer.parseInt(idParam);
                autorizacao.setId(id);
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    autorizacaoMenuDao.update(autorizacao);
                    operacaoLogDao.registrar(usuarioLogado, "Autorizações",
                            "Editar autorização: " + autorizacao.getTelaCodigo() + " / nível " + autorizacao.getNivel());
                    response.sendRedirect(request.getContextPath() + "/autorizacoes?atualizado=1");
                } else {
                    autorizacaoMenuDao.create(autorizacao);
                    operacaoLogDao.registrar(usuarioLogado, "Autorizações",
                            "Criar autorização: " + autorizacao.getTelaCodigo() + " / nível " + autorizacao.getNivel());
                    response.sendRedirect(request.getContextPath() + "/autorizacoes?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar a autorização: "
                        + ErrorMessages.friendly(LOGGER, "autorizacoes", "Salvar autorização", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("autorizacao", autorizacao);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("userTypes", userTypeDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "autorizacoes", "Carregar tipos de usuário para formulário", e);
            request.setAttribute("userTypes", java.util.Collections.emptyList());
        }
        try {
            // Comeca com todas as telas conhecidas do app (mesmo as que ainda nao tem nenhuma
            // linha na tabela, para permitir criar a primeira autorizacao delas) e sobrepoe com
            // a descricao ja cadastrada de cada tela_codigo existente, para o formulario preencher
            // sozinho a descricao ao digitar o codigo de uma tela que ja tem outras autorizacoes.
            // TreeMap: datalist ordenada alfabeticamente pelo codigo da tela.
            Map<String, String> descricoesPorTela = new TreeMap<>(SchemaInitializer.telasConhecidas());
            for (AutorizacaoMenu a : autorizacaoMenuDao.listAll()) {
                descricoesPorTela.put(a.getTelaCodigo(), a.getTelaDescricao());
            }
            request.setAttribute("descricoesPorTela", descricoesPorTela);
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "autorizacoes", "Carregar descrições de telas existentes", e);
            request.setAttribute("descricoesPorTela", new TreeMap<>(SchemaInitializer.telasConhecidas()));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/autorizacaoMenuForm.jsp");
        dispatcher.forward(request, response);
    }
}
