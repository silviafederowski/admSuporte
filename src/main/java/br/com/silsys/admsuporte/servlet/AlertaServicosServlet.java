package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.model.UserType;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Exibido logo apos o login, antes do menu: avisa sobre servicos de manutencao atrasados
 * para usuarios com nivel <= NIVEL_MAXIMO_ALERTA_ATRASO. Se nao houver nenhum atrasado
 * (ou o nivel do usuario nao se aplicar), passa direto para o menu.
 */
public class AlertaServicosServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AlertaServicosServlet.class.getName());

    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object nivelAttr = session != null ? session.getAttribute("userNivel") : null;
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : UserType.NIVEL_MAXIMO_ALERTA_ATRASO + 1;

        List<Servico> servicosAtrasados = new ArrayList<>();
        if (nivel <= UserType.NIVEL_MAXIMO_ALERTA_ATRASO) {
            try {
                for (Servico servico : servicoDao.listAll()) {
                    if (servico.isAtrasado()) {
                        servicosAtrasados.add(servico);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao verificar servicos atrasados.", e);
            }
        }

        if (servicosAtrasados.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        request.setAttribute("servicosAtrasados", servicosAtrasados);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/alertaServicos.jsp");
        dispatcher.forward(request, response);
    }
}
