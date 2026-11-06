package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
import br.com.silsys.admsuporte.dao.ChecklistPortariaDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Tela pos-login. O acesso ja e garantido pelo AuthFilter mapeado em /menu. */
public class MenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MenuServlet.class.getName());

    private static final String TELA_CHECKLIST = "checklist-portaria";

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private final ChecklistPortariaDao checklistDao = new ChecklistPortariaDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("registered") != null) {
            request.setAttribute("infoMessage", "Usuário criado com sucesso.");
        }

        Object nivelAttr = request.getSession().getAttribute("userNivel");
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : Integer.MAX_VALUE;
        Set<String> telasPermitidas;
        try {
            telasPermitidas = autorizacaoMenuDao.listarTelasPermitidas(nivel);
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "menu", "Carregar autorizações do menu", e);
            telasPermitidas = Collections.emptySet();
        }
        // Checklist so aparece se houver item para o tipo do usuario (ou algum item, para quem ve todos).
        if (telasPermitidas.contains(TELA_CHECKLIST)) {
            boolean temItens;
            try {
                temItens = checklistDao.existemItens(ChecklistPortariaFormServlet.tipoDosItens(request, userDao));
            } catch (SQLException e) {
                ErrorMessages.logErro(LOGGER, "menu", "Verificar itens de checklist do usuário", e);
                temItens = false;
            }
            if (!temItens) {
                telasPermitidas = new HashSet<>(telasPermitidas);
                telasPermitidas.remove(TELA_CHECKLIST);
            }
        }
        request.setAttribute("telasPermitidas", telasPermitidas);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/menu.jsp");
        dispatcher.forward(request, response);
    }
}
