package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.AutorizacaoMenu;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lista as autorizacoes de menu (tabela autorizacoes_menu). Regra fixa, independente da propria
 * tabela (para nao criar um problema de "galinha e ovo"): so o nivel 0 acessa a tela (consulta e
 * edicao); qualquer outro nivel volta para o menu - ver tambem AutorizacaoMenuFormServlet e
 * AutorizacaoMenuDeleteServlet.
 */
public class AutorizacaoMenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AutorizacaoMenuServlet.class.getName());
    private static final int NIVEL_SINDICO = 0;

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();

    /** Unico nivel com acesso as telas de Autorizacoes. */
    static boolean isNivelSindico(HttpServletRequest request) {
        Object nivelAttr = request.getSession().getAttribute("userNivel");
        return nivelAttr instanceof Integer && (Integer) nivelAttr == NIVEL_SINDICO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isNivelSindico(request)) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Autorização criada com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Autorização atualizada com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Autorização excluída com sucesso.");
        }

        try {
            List<AutorizacaoMenu> autorizacoes = autorizacaoMenuDao.listAll();
            request.setAttribute("autorizacoes", autorizacoes);
            request.setAttribute("userTypes", userTypeDao.listAll());
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar as autorizações: "
                    + ErrorMessages.friendly(LOGGER, "autorizacoes", "Listar autorizações", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/autorizacaoMenuList.jsp");
        dispatcher.forward(request, response);
    }
}
