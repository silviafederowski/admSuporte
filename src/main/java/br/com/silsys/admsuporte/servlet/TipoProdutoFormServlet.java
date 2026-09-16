package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.TipoProdutoDao;
import br.com.silsys.admsuporte.model.TipoProduto;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Formulario de criacao/edicao de tipo de produto. Acesso controlado pela tela "tipos-produtos". */
public class TipoProdutoFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoProdutoFormServlet.class.getName());

    private final TipoProdutoDao tipoProdutoDao = new TipoProdutoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        TipoProduto tipo = new TipoProduto();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                tipo = tipoProdutoDao.findById(Integer.parseInt(idParam));
                if (tipo == null) {
                    response.sendRedirect(request.getContextPath() + "/tipos-produtos");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "tipos-produtos", "Carregar tipo de produto para edição", e);
                response.sendRedirect(request.getContextPath() + "/tipos-produtos");
                return;
            }
        }
        request.setAttribute("tipo", tipo);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/tipoProdutoForm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String descricao = request.getParameter("descricao");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(descricao)) {
            errors.put("descricao", "Informe a descrição.");
        }

        TipoProduto tipo = new TipoProduto();
        tipo.setDescricao(descricao);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                tipo.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    tipoProdutoDao.update(tipo);
                    operacaoLogDao.registrar(usuarioLogado, "Tipos de produtos", "Editar tipo de produto: " + tipo.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/tipos-produtos?atualizado=1");
                } else {
                    tipoProdutoDao.create(tipo);
                    operacaoLogDao.registrar(usuarioLogado, "Tipos de produtos", "Criar tipo de produto: " + tipo.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/tipos-produtos?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o tipo de produto: "
                        + ErrorMessages.friendly(LOGGER, "tipos-produtos", "Salvar tipo de produto", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("tipo", tipo);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/tipoProdutoForm.jsp");
        dispatcher.forward(request, response);
    }
}
