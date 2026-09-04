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
 * tabela (para nao criar um problema de "galinha e ovo"): qualquer usuario logado pode consultar;
 * so o sindico (administrador, nivel 0) pode editar - ver AutorizacaoMenuFormServlet.
 */
public class AutorizacaoMenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AutorizacaoMenuServlet.class.getName());

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
