package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Exibido logo apos o login, antes do menu: avisa sobre servicos de manutencao atrasados
 * (nivel <= NIVEL_MAXIMO_ALERTA_ATRASO) e sobre servicos agendados para hoje (nivel <=
 * NIVEL_MAXIMO_ALERTA_AGENDADO, mais restrito: so administrador/zelador). Se nao houver nada
 * para mostrar (ou o nivel do usuario nao se aplicar a nenhum dos dois), passa direto para o menu.
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
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : Integer.MAX_VALUE;

        boolean verificaAtrasados = nivel <= UserType.NIVEL_MAXIMO_ALERTA_ATRASO;
        boolean verificaAgendados = nivel <= UserType.NIVEL_MAXIMO_ALERTA_AGENDADO;

        List<Servico> servicosAtrasados = new ArrayList<>();
        List<Servico> servicosAgendadosHoje = new ArrayList<>();
        if (verificaAtrasados || verificaAgendados) {
            try {
                for (Servico servico : servicoDao.listAll()) {
                    if (verificaAtrasados && servico.isAtrasado()) {
                        servicosAtrasados.add(servico);
                    }
                    if (verificaAgendados && servico.isAgendadoParaHoje()) {
                        servicosAgendadosHoje.add(servico);
                    }
                }
            } catch (SQLException e) {
                ErrorMessages.logErro(LOGGER, "alerta-servicos", "Verificar serviços atrasados/agendados", e);
            }
        }

        if (servicosAtrasados.isEmpty() && servicosAgendadosHoje.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        request.setAttribute("servicosAtrasados", servicosAtrasados);
        request.setAttribute("servicosAgendadosHoje", servicosAgendadosHoje);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/alertaServicos.jsp");
        dispatcher.forward(request, response);
    }
}
