package br.com.silsys.admsuporte.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exibe um arquivo da pasta "ParaWeb" do Google Drive para consulta (iframe de preview). */
public class ProcedimentoVisualizarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        String nome = request.getParameter("nome");
        String mimeType = request.getParameter("mimeType");
        if (id == null || id.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/procedimentos");
            return;
        }
        request.setAttribute("arquivoId", id.trim());
        request.setAttribute("arquivoNome", nome);
        request.setAttribute("arquivoMimeType", mimeType);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/procedimentoVisualizar.jsp");
        dispatcher.forward(request, response);
    }
}
