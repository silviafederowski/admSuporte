package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistPortariaDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.model.ChecklistCabecalho;
import br.com.silsys.admsuporte.model.ChecklistPortaria;
import br.com.silsys.admsuporte.model.ChecklistPortariaResumo;
import br.com.silsys.admsuporte.model.Unidade;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checklist. Sem parametros: checklist novo, ja com a data/hora atuais (fuso do condominio), o
 * usuario logado (so exibido, nunca vem do formulario) e os itens do tipo de usuario dele (todos,
 * ate o nivel NIVEL_MAXIMO_CHECKLIST_TODOS). Com data+usuario: os itens daquele checklist, para
 * checar/anular a checagem; cada item alterado ganha a data/hora da alteracao (alterado_em) e uma
 * linha no log de operacoes.
 */
public class ChecklistPortariaFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistPortariaFormServlet.class.getName());
    private static final String TELA_LOG = "Checklist";

    private final ChecklistPortariaDao checklistDao = new ChecklistPortariaDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();
    private final UserDao userDao = new UserDao();
    private final UnidadeDao unidadeDao = new UnidadeDao();

    /**
     * Tipo de usuario cujos itens o usuario logado faz no checklist: null = todos os itens (tipos de
     * UserType.TIPOS_CHECKLIST_TODOS, que tambem veem os checklists de todo mundo); 0 = nenhum
     * (usuario nao encontrado). Lido do cadastro, e nao da sessao, para refletir uma troca de tipo
     * sem precisar sair e entrar de novo.
     */
    static Integer tipoDosItens(HttpServletRequest request, UserDao userDao) throws SQLException {
        Object userId = request.getSession().getAttribute("userId");
        User user = userId instanceof Integer ? userDao.findById((Integer) userId) : null;
        if (user == null) {
            return 0;
        }
        String tipo = user.getUserTypeName() == null ? "" : user.getUserTypeName().trim().toLowerCase();
        return UserType.TIPOS_CHECKLIST_TODOS.contains(tipo) ? null : user.getUserTypeId();
    }

    /**
     * O que o usuario logado pode fazer num checklist gravado. Abrem: o autor, quem ve todos e os
     * colegas do mesmo tipo do autor. Alteram itens/encomendas/status: autor e quem ve todos.
     * Validam: colegas e quem ve todos, nunca o autor, e so com status "disponivel". Depois de
     * validado, ninguem altera mais nada.
     */
    private static final class Permissoes {
        boolean abrir;
        boolean editar;
        boolean verificar;
    }

    private Permissoes permissoes(HttpServletRequest request, String usuarioChecklist, ChecklistCabecalho cabecalho)
            throws SQLException {
        Permissoes p = new Permissoes();
        if (usuarioChecklist == null) {
            return p;
        }
        boolean dono = usuarioChecklist.equals(usuarioLogado(request));
        Integer tipoLogado = tipoDosItens(request, userDao);
        boolean veTodos = tipoLogado == null;
        User autor = userDao.findByNome(usuarioChecklist);
        Integer tipoAutor = autor != null ? autor.getUserTypeId() : null;
        boolean colega = !dono && !veTodos && tipoAutor != null && tipoAutor.equals(tipoLogado);
        boolean validada = cabecalho != null && cabecalho.isVerificado();
        boolean disponivel = cabecalho != null && ChecklistCabecalho.DISPONIVEL.equals(cabecalho.getStatus());
        p.abrir = dono || veTodos || colega;
        p.editar = (dono || veTodos) && !validada;
        p.verificar = !dono && (veTodos || colega) && disponivel;
        return p;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dataParam = request.getParameter("data");
        String usuarioParam = request.getParameter("usuario");
        try {
            if (!ValidationUtil.isBlank(dataParam) && !ValidationUtil.isBlank(usuarioParam)) {
                LocalDate data = LocalDate.parse(dataParam.trim());
                List<ChecklistPortaria> itens = checklistDao.listByDataUsuario(data, usuarioParam);
                if (itens.isEmpty() || !prepararExistente(request, itens)) {
                    response.sendRedirect(request.getContextPath() + "/checklist-portaria");
                    return;
                }
                if (request.getParameter("verificado") != null) {
                    request.setAttribute("infoMessage", "Validação registrada. O checklist não pode mais ser alterado.");
                }
                // Tela de validacao (icone da lista): so para quem pode validar; o resto fica so leitura.
                if (request.getParameter("validar") != null) {
                    if (!Boolean.TRUE.equals(request.getAttribute("podeVerificar"))) {
                        request.setAttribute("formError", "Este checklist não está disponível para você validar "
                                + "(só com o status \"Disponível para validação\", por um colega do autor).");
                    } else {
                        request.setAttribute("modoValidacao", true);
                        request.setAttribute("podeEditar", false);
                    }
                }
            } else {
                String usuario = usuarioLogado(request);
                LocalDate hoje = DateFormatUtil.hoje();
                // Um checklist por usuario e dia: se o de hoje ja existe, abre ele em vez de outro novo.
                if (checklistDao.existe(hoje, usuario)) {
                    response.sendRedirect(urlExistente(request, hoje, usuario));
                    return;
                }
                prepararNovo(request, hoje, DateFormatUtil.agora().toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                        usuario, Collections.emptySet());
            }
        } catch (DateTimeParseException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "checklist-portaria", "Carregar checklist", e);
            response.sendRedirect(request.getContextPath() + "/checklist-portaria");
            return;
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("editar".equals(request.getParameter("modo"))) {
            salvarExistente(request, response);
        } else if ("validar".equals(request.getParameter("modo"))) {
            salvarValidacao(request, response);
        } else {
            salvarNovo(request, response);
        }
    }

    private void salvarNovo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = usuarioLogado(request);
        Set<Integer> checados = idsMarcados(request);
        LocalDate data = null;
        LocalTime hora = null;
        String erro = null;
        try {
            data = LocalDate.parse(request.getParameter("data").trim());
        } catch (DateTimeParseException | NullPointerException e) {
            erro = "Informe uma data válida.";
        }
        try {
            hora = LocalTime.parse(request.getParameter("hora").trim());
        } catch (DateTimeParseException | NullPointerException e) {
            if (erro == null) {
                erro = "Informe uma hora válida.";
            }
        }
        Integer encomendas = lerEncomendas(request);
        if (erro == null && encomendas == null) {
            erro = ERRO_ENCOMENDAS;
        }

        try {
            Map<Integer, Integer> estoque = lerEstoque(request);
            if (erro == null && estoque == null) {
                erro = (String) request.getAttribute("erroEstoque");
            }
            List<ChecklistPortaria> itens = checklistDao.listItensParaNovo(tipoDosItens(request, userDao));
            if (erro == null && itens.isEmpty()) {
                erro = "Não há itens de checklist para o seu tipo de usuário.";
            }
            if (erro == null && checklistDao.existe(data, usuario)) {
                // Nao inclui outro: o aviso da tela (checklistPortariaForm.jsp) traz o link para alterar o existente.
                erro = "Já existe um checklist seu em " + DateFormatUtil.formatar(data)
                        + ". Não é possível incluir outro na mesma data: altere o checklist que já existe.";
                request.setAttribute("dataExistente", data);
            }
            if (erro == null) {
                List<Integer> itemIds = new ArrayList<>();
                for (ChecklistPortaria item : itens) {
                    itemIds.add(item.getItemId());
                }
                Map<Integer, String> obs = observacoes(request, itemIds);
                checklistDao.criar(data, hora, usuario, itemIds, semObservacao(checados, obs), obs, encomendas, estoque,
                        lerStatus(request));
                operacaoLogDao.registrar(usuario, TELA_LOG, "Criar checklist de " + DateFormatUtil.formatar(data)
                        + " (" + contarMarcados(itemIds, checados) + " de " + itemIds.size() + " itens checados; "
                        + encomendas + " encomenda(s) sem baixa no app; " + soma(estoque) + " em estoque)");
                response.sendRedirect(request.getContextPath() + "/checklist-portaria?criado=1");
                return;
            }
        } catch (SQLException e) {
            erro = "Não foi possível gravar o checklist: "
                    + ErrorMessages.friendly(LOGGER, "checklist-portaria", "Gravar checklist", e);
        }

        request.setAttribute("formError", erro);
        try {
            prepararNovo(request, data != null ? data : DateFormatUtil.hoje(),
                    hora != null ? hora : DateFormatUtil.agora().toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                    usuario, checados);
            request.setAttribute("encomendas", request.getParameter("encomendasSemBaixa"));
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "checklist-portaria", "Recarregar itens do checklist", e);
        }
        forward(request, response);
    }

    private void salvarExistente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuarioLogado = usuarioLogado(request);
        String usuarioChecklist = request.getParameter("usuario");
        LocalDate data;
        try {
            data = LocalDate.parse(request.getParameter("data").trim());
        } catch (DateTimeParseException | NullPointerException e) {
            response.sendRedirect(request.getContextPath() + "/checklist-portaria");
            return;
        }

        Integer encomendas = lerEncomendas(request);
        try {
            Permissoes p = permissoes(request, usuarioChecklist, checklistDao.buscarCabecalho(data, usuarioChecklist));
            if (!p.editar) {
                // Sem permissao ou ja verificado (travado): volta para a tela, que mostra o motivo.
                response.sendRedirect(urlExistente(request, data, usuarioChecklist));
                return;
            }
            Map<Integer, Integer> estoque = lerEstoque(request);
            if (encomendas == null || estoque == null) {
                request.setAttribute("formError", encomendas == null ? ERRO_ENCOMENDAS : request.getAttribute("erroEstoque"));
                prepararExistente(request, checklistDao.listByDataUsuario(data, usuarioChecklist));
                request.setAttribute("encomendas", request.getParameter("encomendasSemBaixa"));
                forward(request, response);
                return;
            }
            Map<Integer, Integer> estoqueAntes = checklistDao.buscarEncomendasEstoque(data, usuarioChecklist);
            if (!estoque.equals(estoqueAntes)) {
                checklistDao.salvarEncomendasEstoque(data, usuarioChecklist, estoque);
                operacaoLogDao.registrar(usuarioLogado, TELA_LOG, "Alterar encomendas em estoque: "
                        + soma(estoqueAntes) + " -> " + soma(estoque)
                        + " (checklist de " + DateFormatUtil.formatar(data) + " - " + usuarioChecklist + ")");
            }
            ChecklistCabecalho cabecalhoAntes = checklistDao.buscarCabecalho(data, usuarioChecklist);
            String status = lerStatus(request);
            if (cabecalhoAntes != null && !status.equals(cabecalhoAntes.getStatus())) {
                checklistDao.atualizarStatus(data, usuarioChecklist, status);
                operacaoLogDao.registrar(usuarioLogado, TELA_LOG, "Alterar status: "
                        + cabecalhoAntes.getStatusLabel() + " -> " + ChecklistCabecalho.statusLabel(status)
                        + " (checklist de " + DateFormatUtil.formatar(data) + " - " + usuarioChecklist + ")");
            }
            Integer encomendasAntes = checklistDao.buscarEncomendas(data, usuarioChecklist);
            if (!encomendas.equals(encomendasAntes)) {
                checklistDao.salvarEncomendas(data, usuarioChecklist, encomendas);
                operacaoLogDao.registrar(usuarioLogado, TELA_LOG, "Alterar encomendas sem baixa: "
                        + (encomendasAntes == null ? "não informado" : encomendasAntes) + " -> " + encomendas
                        + " (checklist de " + DateFormatUtil.formatar(data) + " - " + usuarioChecklist + ")");
            }
            List<Integer> idsLinhas = new ArrayList<>();
            for (ChecklistPortaria c : checklistDao.listByDataUsuario(data, usuarioChecklist)) {
                idsLinhas.add(c.getId());
            }
            Map<Integer, String> obs = observacoes(request, idsLinhas);
            List<ChecklistPortaria> alterados = checklistDao.atualizar(data, usuarioChecklist,
                    semObservacao(idsMarcados(request), obs), obs, DateFormatUtil.agora());
            String sufixo = " (checklist de " + DateFormatUtil.formatar(data) + " - " + usuarioChecklist + ")";
            for (ChecklistPortaria c : alterados) {
                if (c.isChecadoMudou()) {
                    operacaoLogDao.registrar(usuarioLogado, TELA_LOG,
                            (c.isChecado() ? "Checar item: " : "Anular checagem: ") + c.getItemDescricao() + sufixo);
                }
                if (c.isObservacaoMudou()) {
                    operacaoLogDao.registrar(usuarioLogado, TELA_LOG,
                            "Alterar observação do item: " + c.getItemDescricao() + sufixo);
                }
            }
            response.sendRedirect(request.getContextPath() + "/checklist-portaria?atualizado=1");
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível atualizar o checklist: "
                    + ErrorMessages.friendly(LOGGER, "checklist-portaria", "Atualizar checklist", e));
            try {
                prepararExistente(request, checklistDao.listByDataUsuario(data, usuarioChecklist));
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "checklist-portaria", "Recarregar checklist", e2);
            }
            forward(request, response);
        }
    }

    /**
     * Conclui a validacao (tela aberta pelo icone da lista). Quem valida e sempre o usuario logado e
     * a data/hora e o momento do registro. Em cada item: confirmado (val_<id>) ou a observacao de
     * quem validou (valobs_<id>), nunca os dois, e todo item precisa de um deles. So com status
     * "disponivel"; depois o status vira "validada" e o checklist fica travado.
     */
    private void salvarValidacao(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuarioChecklist = request.getParameter("usuario");
        LocalDate data;
        try {
            data = LocalDate.parse(request.getParameter("data").trim());
        } catch (DateTimeParseException | NullPointerException e) {
            response.sendRedirect(request.getContextPath() + "/checklist-portaria");
            return;
        }
        String obsGeral = request.getParameter("verificacaoObs");
        obsGeral = ValidationUtil.isBlank(obsGeral) ? null : obsGeral.trim();
        if (obsGeral != null && obsGeral.length() > MAX_OBSERVACAO) {
            obsGeral = obsGeral.substring(0, MAX_OBSERVACAO);
        }

        try {
            Permissoes p = permissoes(request, usuarioChecklist, checklistDao.buscarCabecalho(data, usuarioChecklist));
            if (!p.verificar) {
                response.sendRedirect(urlExistente(request, data, usuarioChecklist));
                return;
            }
            List<ChecklistPortaria> linhas = checklistDao.listByDataUsuario(data, usuarioChecklist);
            List<Integer> ids = new ArrayList<>();
            for (ChecklistPortaria c : linhas) {
                ids.add(c.getId());
            }
            Map<Integer, String> obsValidacao = observacoes(request, ids, "valobs_");
            Set<Integer> confirmados = semObservacao(idsMarcados(request, "val"), obsValidacao);

            String erro = null;
            for (ChecklistPortaria c : linhas) {
                if (!confirmados.contains(c.getId()) && !obsValidacao.containsKey(c.getId())) {
                    erro = "Em cada item, confirme a validação ou escreva o motivo de não confirmar. Falta: "
                            + c.getItemDescricao();
                    break;
                }
            }
            String validador = usuarioLogado(request);
            if (erro == null && !checklistDao.validar(data, usuarioChecklist, confirmados, obsValidacao,
                    validador, DateFormatUtil.agora(), obsGeral)) {
                erro = "Este checklist não está mais disponível para validação.";
            }
            if (erro == null) {
                operacaoLogDao.registrar(validador, TELA_LOG, "Validar checklist de " + DateFormatUtil.formatar(data)
                        + " - " + usuarioChecklist + " (" + confirmados.size() + " de " + linhas.size()
                        + " itens confirmados)");
                response.sendRedirect(urlExistente(request, data, usuarioChecklist) + "&verificado=1");
                return;
            }
            request.setAttribute("formError", erro);
            prepararExistente(request, linhas);
            request.setAttribute("modoValidacao", true);
            request.setAttribute("podeEditar", false);
            request.setAttribute("verificacaoObsDigitada", obsGeral);
            // Volta com o que foi marcado/digitado na validacao.
            for (ChecklistPortaria c : linhas) {
                c.setValidado(confirmados.contains(c.getId()) ? Boolean.TRUE : null);
                c.setValidacaoObs(obsValidacao.get(c.getId()));
            }
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível registrar a validação: "
                    + ErrorMessages.friendly(LOGGER, "checklist-portaria", "Validar checklist", e));
            try {
                prepararExistente(request, checklistDao.listByDataUsuario(data, usuarioChecklist));
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "checklist-portaria", "Recarregar checklist", e2);
            }
        }
        forward(request, response);
    }

    /** Status escolhido pelo autor: so "em_andamento" ou "disponivel" ("validada" so pela validacao). */
    private String lerStatus(HttpServletRequest request) {
        return ChecklistCabecalho.DISPONIVEL.equals(request.getParameter("status"))
                ? ChecklistCabecalho.DISPONIVEL : ChecklistCabecalho.EM_ANDAMENTO;
    }

    private void prepararNovo(HttpServletRequest request, LocalDate data, LocalTime hora, String usuario,
                              Set<Integer> checados) throws SQLException {
        List<ChecklistPortaria> itens = checklistDao.listItensParaNovo(tipoDosItens(request, userDao));
        for (ChecklistPortaria item : itens) {
            item.setChecado(checados.contains(item.getItemId()));
            // Volta com a observacao digitada, se o formulario foi reexibido por erro.
            item.setObservacao(request.getParameter("obs_" + item.getItemId()));
        }
        // Datas em que o usuario ja tem checklist: a tela avisa assim que uma delas for escolhida.
        List<String> datasExistentes = new ArrayList<>();
        for (ChecklistPortariaResumo r : checklistDao.listResumos(usuario, null)) {
            datasExistentes.add(r.getData().toString());
        }
        request.setAttribute("novo", true);
        request.setAttribute("data", data);
        request.setAttribute("hora", DateFormatUtil.formatarHora(hora));
        request.setAttribute("usuarioChecklist", usuario);
        request.setAttribute("itens", itens);
        request.setAttribute("datasExistentes", String.join(",", datasExistentes));
        prepararEstoque(request, Collections.emptyMap());
    }

    /** Prepara a tela de um checklist gravado; false se o usuario logado nao pode abri-lo. */
    private boolean prepararExistente(HttpServletRequest request, List<ChecklistPortaria> itens) throws SQLException {
        ChecklistPortaria primeiro = itens.get(0);
        ChecklistCabecalho cabecalho = checklistDao.buscarCabecalho(primeiro.getData(), primeiro.getUsuario());
        Permissoes p = permissoes(request, primeiro.getUsuario(), cabecalho);
        if (!p.abrir) {
            return false;
        }
        request.setAttribute("novo", false);
        request.setAttribute("data", primeiro.getData());
        request.setAttribute("dataFormatada", primeiro.getDataFormatada());
        request.setAttribute("hora", primeiro.getHoraFormatada());
        request.setAttribute("usuarioChecklist", primeiro.getUsuario());
        request.setAttribute("itens", itens);
        request.setAttribute("encomendas", cabecalho != null ? cabecalho.getEncomendasSemBaixa() : null);
        request.setAttribute("cabecalho", cabecalho);
        prepararEstoque(request, checklistDao.buscarEncomendasEstoque(primeiro.getData(), primeiro.getUsuario()));
        request.setAttribute("podeEditar", p.editar);
        request.setAttribute("podeVerificar", p.verificar);
        return true;
    }

    private static final String ERRO_ENCOMENDAS =
            "Informe quantas encomendas estão no aplicativo sem baixa (número inteiro de 0 a 999).";

    private static final int MAX_OBSERVACAO = 1000;

    /**
     * Observacao de cada linha, do campo "obs_<id>" (item_id no checklist novo, id da linha no
     * existente). Vazia vira null; texto maior que a coluna e cortado.
     */
    private Map<Integer, String> observacoes(HttpServletRequest request, List<Integer> ids) {
        return observacoes(request, ids, "obs_");
    }

    /** Mesma leitura, com outro prefixo de campo (ex.: "valobs_" para a observacao da validacao). */
    private Map<Integer, String> observacoes(HttpServletRequest request, List<Integer> ids, String prefixo) {
        Map<Integer, String> result = new HashMap<>();
        for (Integer id : ids) {
            String valor = request.getParameter(prefixo + id);
            if (!ValidationUtil.isBlank(valor)) {
                String texto = valor.trim();
                result.put(id, texto.length() > MAX_OBSERVACAO ? texto.substring(0, MAX_OBSERVACAO) : texto);
            }
        }
        return result;
    }

    /**
     * Encomendas em estoque por unidade, dos campos "estoque_<chave>": vazio = 0, inteiro de 0 a 999.
     * Devolve null (e poe a mensagem em request "erroEstoque") se alguma quantidade for invalida.
     */
    private Map<Integer, Integer> lerEstoque(HttpServletRequest request) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        for (Unidade u : unidadeDao.listAll()) {
            String valor = request.getParameter("estoque_" + u.getChave());
            if (ValidationUtil.isBlank(valor)) {
                continue;
            }
            try {
                int n = Integer.parseInt(valor.trim());
                if (n < 0 || n > 999) {
                    throw new NumberFormatException();
                }
                if (n > 0) {
                    result.put(u.getChave(), n);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("erroEstoque", "Quantidade de encomendas em estoque inválida na unidade "
                        + u.getCodigo() + " (número inteiro de 0 a 999).");
                return null;
            }
        }
        return result;
    }

    /**
     * Unidades e as quantidades a exibir na tabela de estoque (chave como texto -> valor): o que
     * veio no formulario, se ele foi reexibido por erro, senao o gravado.
     */
    private void prepararEstoque(HttpServletRequest request, Map<Integer, Integer> gravado) throws SQLException {
        List<Unidade> unidades = unidadeDao.listAll();
        Map<String, String> valores = new HashMap<>();
        for (Unidade u : unidades) {
            String digitado = request.getParameter("estoque_" + u.getChave());
            Integer salvo = gravado.get(u.getChave());
            valores.put(String.valueOf(u.getChave()),
                    digitado != null ? digitado.trim() : (salvo != null ? String.valueOf(salvo) : ""));
        }
        request.setAttribute("unidades", unidades);
        request.setAttribute("estoqueValores", valores);
    }

    private static int soma(Map<Integer, Integer> porUnidade) {
        int total = 0;
        for (Integer n : porUnidade.values()) {
            total += n;
        }
        return total;
    }

    /**
     * Item checado nao tem observacao, e observacao desfaz a checagem: os itens com observacao
     * saem dos checados. A tela ja impede os dois juntos; isto garante a regra no servidor.
     */
    private Set<Integer> semObservacao(Set<Integer> checados, Map<Integer, String> observacoes) {
        Set<Integer> result = new HashSet<>(checados);
        result.removeAll(observacoes.keySet());
        return result;
    }

    /** Campo obrigatorio "encomendasSemBaixa": inteiro de 0 a 999, ou null se vazio/invalido. */
    private Integer lerEncomendas(HttpServletRequest request) {
        String valor = request.getParameter("encomendasSemBaixa");
        if (ValidationUtil.isBlank(valor)) {
            return null;
        }
        try {
            int n = Integer.parseInt(valor.trim());
            return n >= 0 && n <= 999 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Valores marcados em "checado": item_id no checklist novo, id da linha no existente. */
    private Set<Integer> idsMarcados(HttpServletRequest request) {
        return idsMarcados(request, "checado");
    }

    /** Mesma leitura, de outro campo de caixas (ex.: "val", confirmacao da validacao por id da linha). */
    private Set<Integer> idsMarcados(HttpServletRequest request, String campo) {
        Set<Integer> ids = new HashSet<>();
        String[] valores = request.getParameterValues(campo);
        if (valores != null) {
            for (String v : valores) {
                try {
                    ids.add(Integer.parseInt(v.trim()));
                } catch (NumberFormatException e) {
                    // ignora valores invalidos
                }
            }
        }
        return ids;
    }

    private int contarMarcados(List<Integer> itemIds, Set<Integer> checados) {
        int total = 0;
        for (Integer id : itemIds) {
            if (checados.contains(id)) {
                total++;
            }
        }
        return total;
    }

    private String usuarioLogado(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("userName");
    }

    private String urlExistente(HttpServletRequest request, LocalDate data, String usuario)
            throws UnsupportedEncodingException {
        // Projeto compila para Java 8: sem o encode(String, Charset), que e do Java 10+.
        return request.getContextPath() + "/checklist-portaria/form?data=" + data
                + "&usuario=" + URLEncoder.encode(usuario, "UTF-8");
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/checklistPortariaForm.jsp");
        dispatcher.forward(request, response);
    }
}
