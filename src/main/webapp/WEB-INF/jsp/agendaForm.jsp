<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "agenda") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Compromisso - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=48">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar compromisso</c:when>
                    <c:when test="${empty param.id}">Novo compromisso</c:when>
                    <c:otherwise>Editar compromisso</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="agendaForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/agenda" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="agendaExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <c:set var="logTela" value="Agenda" />
                <c:set var="logRegistro" value="${empty param.id ? '' : 'agenda:'.concat(param.id)}" />
                <%@ include file="logsIcone.jspf" %>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="agendaForm" method="post" action="${pageContext.request.contextPath}/agenda/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row">
                <div class="field">
                    <label for="data">Data</label>
                    <input type="date" id="data" name="data" value="${agendaItem.data}" ${dis}>
                    <c:if test="${not empty errors.data}"><div class="field-error">${errors.data}</div></c:if>
                </div>
                <div class="field">
                    <label for="hora">Hora</label>
                    <%-- Horarios das 07:00 as 19:00, de meia em meia hora; uma hora antiga fora dessa grade
                         aparece como opcao extra (e continua aceita enquanto nao for trocada). --%>
                    <c:set var="horaAtual" value="${agendaItem.horaFormatada}" />
                    <c:set var="horaNaGrade" value="${empty horaAtual}" />
                    <select id="hora" name="hora" ${dis}>
                        <option value="" ${empty horaAtual ? 'selected' : ''}>Sem hora</option>
                        <c:forEach var="h" begin="7" end="19">
                            <c:forEach var="m" begin="0" end="30" step="30">
                                <c:if test="${h lt 19 || m eq 0}">
                                    <c:set var="hm" value="${h lt 10 ? '0' : ''}${h}:${m eq 0 ? '00' : m}" />
                                    <c:if test="${hm eq horaAtual}"><c:set var="horaNaGrade" value="${true}" /></c:if>
                                    <option value="${hm}" ${hm eq horaAtual ? 'selected' : ''}>${hm}</option>
                                </c:if>
                            </c:forEach>
                        </c:forEach>
                        <c:if test="${!horaNaGrade}">
                            <option value="${horaAtual}" selected>${horaAtual} (fora da grade)</option>
                        </c:if>
                    </select>
                    <c:if test="${not empty errors.hora}"><div class="field-error">${errors.hora}</div></c:if>
                </div>
                <div class="field">
                    <label for="pontoFocal">Ponto focal</label>
                    <input type="text" id="pontoFocal" name="pontoFocal" value="${agendaItem.pontoFocal}" ${dis} required>
                    <c:if test="${not empty errors.pontoFocal}"><div class="field-error">${errors.pontoFocal}</div></c:if>
                </div>
            </div>

            <div class="field">
                <label for="assunto">Assunto</label>
                <input type="text" id="assunto" name="assunto" value="${agendaItem.assunto}" ${dis} required>
                <c:if test="${not empty errors.assunto}"><div class="field-error">${errors.assunto}</div></c:if>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="prioridade">Prioridade</label>
                    <select id="prioridade" name="prioridade" ${dis} required>
                        <option value="" ${empty agendaItem.prioridade ? 'selected' : ''}>Selecione...</option>
                        <option value="alta" ${agendaItem.prioridade.dbValue == 'alta' ? 'selected' : ''}>Alta</option>
                        <option value="media" ${agendaItem.prioridade.dbValue == 'media' ? 'selected' : ''}>Média</option>
                        <option value="baixa" ${agendaItem.prioridade.dbValue == 'baixa' ? 'selected' : ''}>Baixa</option>
                    </select>
                    <c:if test="${not empty errors.prioridade}"><div class="field-error">${errors.prioridade}</div></c:if>
                </div>
                <div class="field">
                    <label for="servicoId">Serviço</label>
                    <select id="servicoId" name="servicoId" ${dis} onchange="atualizarCamposServico()">
                        <option value="" ${empty agendaItem.servicoId ? 'selected' : ''}>Nenhum</option>
                        <c:forEach var="s" items="${servicos}">
                            <option value="${s.id}" ${agendaItem.servicoId == s.id ? 'selected' : ''}>${s.descricao}</option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.servicoId}"><div class="field-error">${errors.servicoId}</div></c:if>
                </div>
                <div class="field">
                    <label for="prestadorId">Prestador</label>
                    <select id="prestadorId" name="prestadorId" ${dis}>
                        <option value="">Selecione...</option>
                    </select>
                    <c:if test="${not empty errors.prestadorId}"><div class="field-error">${errors.prestadorId}</div></c:if>
                    <%-- Prestadores que oferecem cada servico; o script copia para a lista acima os do servico escolhido. --%>
                    <select id="prestadoresPorServico" hidden disabled>
                        <c:forEach var="s" items="${servicos}">
                            <c:forEach var="p" items="${s.prestadoresQueOferecem}">
                                <option data-servico="${s.id}" value="${p.id}">${p.nomeRazaoSocial}</option>
                            </c:forEach>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="status">Status</label>
                    <select id="status" name="status" ${dis} onchange="sugerirDataStatus(); atualizarCamposServico()">
                        <option value="pendente" ${agendaItem.status.dbValue != 'resolvido' && agendaItem.status.dbValue != 'cancelado' ? 'selected' : ''}>Pendente</option>
                        <option value="resolvido" ${agendaItem.status.dbValue == 'resolvido' ? 'selected' : ''}>Resolvido</option>
                        <option value="cancelado" ${agendaItem.status.dbValue == 'cancelado' ? 'selected' : ''}>Cancelado</option>
                    </select>
                    <c:if test="${not empty errors.status}"><div class="field-error">${errors.status}</div></c:if>
                </div>
                <div class="field">
                    <label for="dataStatus">Data do status</label>
                    <input type="date" id="dataStatus" name="dataStatus" value="${agendaItem.dataStatus}" ${dis}>
                    <c:if test="${not empty errors.dataStatus}"><div class="field-error">${errors.dataStatus}</div></c:if>
                </div>
                <div class="field">
                    <label title="Com serviço escolhido e status Resolvido: a data do status e o prestador viram a última execução do serviço">Atualizar serviço</label>
                    <div class="radio-row">
                        <label class="radio-option">
                            <input type="radio" name="atualizarServico" value="nao" ${dis}
                                   ${param.atualizarServico != 'sim' ? 'checked' : ''}>
                            <span>Não</span>
                        </label>
                        <label class="radio-option">
                            <input type="radio" name="atualizarServico" value="sim" ${dis}
                                   ${param.atualizarServico == 'sim' ? 'checked' : ''}>
                            <span>Sim</span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="field">
                <label for="observacao">Observação</label>
                <textarea id="observacao" name="observacao" style="min-height:160px;" ${dis}>${agendaItem.observacao}</textarea>
            </div>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="agendaExcluirForm" method="post" action="${pageContext.request.contextPath}/agenda/excluir"
                  onsubmit="return confirm('Excluir este compromisso?');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
<script>
    var PRESTADOR_SELECIONADO = '${agendaItem.prestadorId}';
    var READ_ONLY = ${readOnly ? 'true' : 'false'};

    function atualizarCamposServico() {
        var servicoId = document.getElementById('servicoId').value;
        var prestadorSelect = document.getElementById('prestadorId');
        var atual = prestadorSelect.value || PRESTADOR_SELECIONADO;
        PRESTADOR_SELECIONADO = '';

        while (prestadorSelect.options.length > 1) {
            prestadorSelect.remove(1);
        }
        document.querySelectorAll('#prestadoresPorServico option').forEach(function (modelo) {
            if (modelo.getAttribute('data-servico') === servicoId) {
                var option = modelo.cloneNode(true);
                option.selected = option.value === String(atual);
                prestadorSelect.add(option);
            }
        });

        var temServico = !!servicoId;
        prestadorSelect.required = temServico && document.getElementById('status').value === 'resolvido';
        if (!READ_ONLY) {
            prestadorSelect.disabled = !temServico;
        }
        atualizarOpcaoAtualizarServico();
    }

    /* "Atualizar serviço" so faz sentido com um servico escolhido e o compromisso resolvido. */
    function atualizarOpcaoAtualizarServico() {
        var habilitado = !!document.getElementById('servicoId').value
            && document.getElementById('status').value === 'resolvido';
        document.querySelectorAll('input[name="atualizarServico"]').forEach(function (radio) {
            if (!READ_ONLY) {
                radio.disabled = !habilitado;
            }
            if (!habilitado) {
                radio.checked = radio.value === 'nao';
            }
        });
    }

    /* Ao marcar como resolvido ou cancelado, sugere a data de hoje (o usuario pode trocar depois). */
    function sugerirDataStatus() {
        if (document.getElementById('status').value === 'pendente') {
            return;
        }
        var hoje = new Date();
        var mes = String(hoje.getMonth() + 1).padStart(2, '0');
        var dia = String(hoje.getDate()).padStart(2, '0');
        document.getElementById('dataStatus').value = hoje.getFullYear() + '-' + mes + '-' + dia;
    }

    atualizarCamposServico();
</script>
<script src="${pageContext.request.contextPath}/js/dataLimpar.js"></script>
</body>
</html>
