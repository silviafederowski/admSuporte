package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Conteudo da janela de log (icone 📜 das telas, ver logsIcone.jspf): ?tela=A,B lista o log dessas
 * telas; ?registro=agenda:12 lista o log daquele registro. So para quem tem acesso a tela "Log de
 * operacoes" (autorizacoes_menu, tela "operacoes"), a mesma regra que decide se o icone aparece.
 */
public class LogsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LogsServlet.class.getName());

    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();
    private static final AutorizacaoMenuDao AUTORIZACAO_MENU_DAO = new AutorizacaoMenuDao();

    /** O usuario logado pode ver o log? (mesma autorizacao da tela "Log de operacoes"). */
    public static boolean podeVerLogs(HttpServletRequest request) {
        Object nivel = request.getSession(false) != null ? request.getSession(false).getAttribute("userNivel") : null;
        if (!(nivel instanceof Integer)) {
            return false;
        }
        try {
            return AUTORIZACAO_MENU_DAO.buscarTipoAcesso("operacoes", (Integer) nivel) != null;
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "logs", "Verificar acesso ao log de operações", e);
            return false;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!podeVerLogs(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String registro = request.getParameter("registro");
        List<String> telas = new ArrayList<>();
        String telasParam = request.getParameter("tela");
        if (telasParam != null) {
            for (String t : telasParam.split(",")) {
                if (!t.trim().isEmpty()) {
                    telas.add(t.trim());
                }
            }
        }
        try {
            if (!ValidationUtil.isBlank(registro)) {
                request.setAttribute("logs", operacaoLogDao.listar(null, registro.trim()));
            } else if (!telas.isEmpty()) {
                request.setAttribute("logs", operacaoLogDao.listar(telas, null));
            } else {
                request.setAttribute("logs", new ArrayList<>());
            }
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar o log: "
                    + ErrorMessages.friendly(LOGGER, "logs", "Listar log da tela", e));
        }
        request.getRequestDispatcher("/WEB-INF/jsp/logsFragmento.jsp").forward(request, response);
    }
}
