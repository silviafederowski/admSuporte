package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistItemDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.ChecklistItem;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.Alteracoes;
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

/** Formulario de criacao/edicao de item de checklist. Acesso controlado pela tela "checklist-itens". */
public class ChecklistItemFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistItemFormServlet.class.getName());
    private static final String TELA_LOG = "Itens de checklist";

    private final ChecklistItemDao checklistItemDao = new ChecklistItemDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        ChecklistItem item = new ChecklistItem();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                item = checklistItemDao.findById(Integer.parseInt(idParam));
                if (item == null) {
                    response.sendRedirect(request.getContextPath() + "/checklist-itens");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "checklist-itens", "Carregar item de checklist para edição", e);
                response.sendRedirect(request.getContextPath() + "/checklist-itens");
                return;
            }
        }
        request.setAttribute("item", item);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String userTypeIdParam = request.getParameter("userTypeId");
        String descricao = request.getParameter("descricao");

        Map<String, String> errors = new HashMap<>();
        UserType tipo = null;
        try {
            tipo = userTypeDao.findById(Integer.parseInt(userTypeIdParam));
        } catch (NumberFormatException | NullPointerException e) {
            // tratado abaixo
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "checklist-itens", "Carregar tipo de usuário do item", e);
        }
        if (tipo == null) {
            errors.put("userTypeId", "Selecione o tipo de usuário.");
        }
        if (ValidationUtil.isBlank(descricao)) {
            errors.put("descricao", "Informe a descrição.");
        }

        ChecklistItem item = new ChecklistItem();
        if (tipo != null) {
            item.setUserTypeId(tipo.getId());
            item.setUserTypeName(tipo.getName());
        }
        item.setDescricao(descricao);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                item.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                String resumo = item.getUserTypeName() + ": " + item.getDescricao();
                if (isEdit) {
                    ChecklistItem antes = checklistItemDao.findById(item.getId());
                    checklistItemDao.update(item);
                    operacaoLogDao.registrar(usuarioLogado, TELA_LOG, "Editar item de checklist: " + resumo, "checklist-item:" + item.getId(),
                            Alteracoes.descrever(antes, checklistItemDao.findById(item.getId())));
                    response.sendRedirect(request.getContextPath() + "/checklist-itens?atualizado=1");
                } else {
                    item.setId(checklistItemDao.create(item));
                    operacaoLogDao.registrar(usuarioLogado, TELA_LOG, "Criar item de checklist: " + resumo, "checklist-item:" + item.getId());
                    response.sendRedirect(request.getContextPath() + "/checklist-itens?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o item: "
                        + ErrorMessages.friendly(LOGGER, "checklist-itens", "Salvar item de checklist", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("item", item);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("userTypes", userTypeDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "checklist-itens", "Carregar tipos de usuário para formulário", e);
            request.setAttribute("userTypes", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/checklistItemForm.jsp");
        dispatcher.forward(request, response);
    }
}
