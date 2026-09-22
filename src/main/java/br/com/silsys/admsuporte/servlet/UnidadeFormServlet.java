package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.model.Unidade;
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

/**
 * Formulario de criacao/edicao de unidade. A chave nao e auto-incrementada (vem preenchida pelo
 * usuario na criacao) e nao pode ser alterada depois. Acesso restrito pela tela "unidades".
 */
public class UnidadeFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UnidadeFormServlet.class.getName());

    private final UnidadeDao unidadeDao = new UnidadeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        Unidade unidade = new Unidade();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                unidade = unidadeDao.findByChave(Integer.parseInt(idParam));
                if (unidade == null) {
                    response.sendRedirect(request.getContextPath() + "/unidades");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "unidades", "Carregar unidade para edição", e);
                response.sendRedirect(request.getContextPath() + "/unidades");
                return;
            }
        }
        request.setAttribute("unidade", unidade);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/unidadeForm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String chaveParam = request.getParameter("chave");
        String codigo = request.getParameter("codigo");
        String vagasGaragemParam = request.getParameter("vagasGaragem");
        boolean candidataIdoso = request.getParameter("candidataIdoso") != null;

        Map<String, String> errors = new HashMap<>();
        boolean isEdit = idParam != null && !idParam.trim().isEmpty();

        Integer chave = null;
        if (isEdit) {
            try {
                chave = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        } else {
            if (ValidationUtil.isBlank(chaveParam)) {
                errors.put("chave", "Informe a chave.");
            } else {
                try {
                    chave = Integer.parseInt(chaveParam.trim());
                } catch (NumberFormatException e) {
                    errors.put("chave", "Informe um número inteiro válido.");
                }
            }
        }

        if (ValidationUtil.isBlank(codigo)) {
            errors.put("codigo", "Informe o código.");
        }

        int vagasGaragem = "2".equals(vagasGaragemParam) ? 2 : 1;

        Unidade unidade = new Unidade();
        if (chave != null) {
            unidade.setChave(chave);
        }
        unidade.setCodigo(codigo);
        unidade.setCandidataIdoso(candidataIdoso);
        unidade.setVagasGaragem(vagasGaragem);

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    unidadeDao.update(unidade);
                    operacaoLogDao.registrar(usuarioLogado, "Unidades", "Editar unidade: " + unidade.getCodigo());
                    response.sendRedirect(request.getContextPath() + "/unidades?atualizado=1");
                } else {
                    unidadeDao.create(unidade);
                    operacaoLogDao.registrar(usuarioLogado, "Unidades", "Criar unidade: " + unidade.getCodigo());
                    response.sendRedirect(request.getContextPath() + "/unidades?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar a unidade: "
                        + ErrorMessages.friendly(LOGGER, "unidades", "Salvar unidade", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("unidade", unidade);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/unidadeForm.jsp");
        dispatcher.forward(request, response);
    }
}
