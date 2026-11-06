package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistPortariaDao;
import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.util.ChecklistPdfGenerator;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * PDF do checklist em branco (paisagem), para preencher a mao: os itens do tipo do usuario logado
 * (todos, para quem ve todos - ver ChecklistPortariaFormServlet.tipoDosItens) e todas as unidades.
 * Acesso controlado pela tela "checklist-portaria".
 */
public class ChecklistPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistPdfServlet.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ChecklistPortariaDao checklistDao = new ChecklistPortariaDao();
    private final UnidadeDao unidadeDao = new UnidadeDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ChecklistPdfGenerator.generate((String) request.getAttribute("nomeCondominio"),
                    checklistDao.listItensParaNovo(ChecklistPortariaFormServlet.tipoDosItens(request, userDao)),
                    unidadeDao.listAll(), buffer);

            String fileName = "checklist-em-branco-" + DateFormatUtil.agora().format(FILE_TIMESTAMP) + ".pdf";
            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            ErrorMessages.logErro(LOGGER, "checklist-portaria", "Gerar PDF do checklist em branco", e);
            response.sendRedirect(request.getContextPath() + "/checklist-portaria");
        }
    }
}
