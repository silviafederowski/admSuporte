package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.model.SpreadsheetRow;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Tela "Formulario Google": mostra o formulario de upload da planilha "Form",
 * ou a lista de linhas ja processadas (se o usuario acabou de fazer upload),
 * para escolher quais unidades entram no PDF. Equivalente a GoogleFormScreen.tsx.
 */
public class GoogleFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        List<SpreadsheetRow> rows = session == null ? null
                : (List<SpreadsheetRow>) session.getAttribute("googleFormRows");

        if (rows != null && !rows.isEmpty()) {
            request.setAttribute("rows", rows);
            request.getRequestDispatcher("/WEB-INF/jsp/googleFormSelect.jsp").forward(request, response);
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleForm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("1".equals(request.getParameter("reset"))) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("googleFormRows");
            }
        }
        response.sendRedirect(request.getContextPath() + "/google-form");
    }
}
