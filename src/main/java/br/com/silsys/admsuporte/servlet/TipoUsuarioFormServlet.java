package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.UserType;
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

/** Formulario de criacao/edicao de tipo de usuario. Acesso controlado pela tela "tipos-usuario". */
public class TipoUsuarioFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoUsuarioFormServlet.class.getName());

    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        UserType tipo = new UserType();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                tipo = userTypeDao.findById(Integer.parseInt(idParam));
                if (tipo == null) {
                    response.sendRedirect(request.getContextPath() + "/tipos-usuario");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "tipos-usuario", "Carregar tipo de usuário para edição", e);
                response.sendRedirect(request.getContextPath() + "/tipos-usuario");
                return;
            }
        }
        request.setAttribute("tipo", tipo);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/tipoUsuarioForm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String name = request.getParameter("name");
        String nivelParam = request.getParameter("nivel");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(name)) {
            errors.put("name", "Informe o nome do tipo de usuário.");
        }
        Integer nivel = null;
        if (ValidationUtil.isBlank(nivelParam)) {
            errors.put("nivel", "Informe o nível.");
        } else {
            String nivelTrimmed = nivelParam.trim();
            try {
                nivel = Integer.parseInt(nivelTrimmed);
                if (nivel < 0) {
                    errors.put("nivel", "O nível não pode ser negativo.");
                } else if (nivel < 10 && (nivelTrimmed.length() != 2 || nivelTrimmed.charAt(0) != '0')) {
                    errors.put("nivel", "Níveis menores que 10 precisam do zero à esquerda (ex.: 05).");
                }
            } catch (NumberFormatException e) {
                errors.put("nivel", "Informe um número válido.");
            }
        }

        UserType tipo = new UserType();
        tipo.setName(name);
        if (nivel != null) {
            tipo.setNivel(nivel);
        }

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                tipo.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    userTypeDao.update(tipo);
                    operacaoLogDao.registrar(usuarioLogado, "Tipos de usuário", "Editar tipo de usuário: " + tipo.getName());
                    response.sendRedirect(request.getContextPath() + "/tipos-usuario?atualizado=1");
                } else {
                    userTypeDao.create(tipo);
                    operacaoLogDao.registrar(usuarioLogado, "Tipos de usuário", "Criar tipo de usuário: " + tipo.getName());
                    response.sendRedirect(request.getContextPath() + "/tipos-usuario?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o tipo de usuário: "
                        + ErrorMessages.friendly(LOGGER, "tipos-usuario", "Salvar tipo de usuário", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("tipo", tipo);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/tipoUsuarioForm.jsp");
        dispatcher.forward(request, response);
    }
}
