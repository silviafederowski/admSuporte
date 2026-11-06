<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Agenda - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=34">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Agenda</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/agenda/form" title="Novo compromisso" aria-label="Novo compromisso">➕</a>
                </c:if>
                <c:if test="${not empty agenda}">
                    <a class="menu-icon-link" href="javascript:void(0)" onclick="abrirDialogoPdf()" title="Gerar PDF da lista filtrada" aria-label="Gerar PDF da lista filtrada">🖨️</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty agenda}">
                <p class="empty-state">Nenhum compromisso cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <%-- Titulos abreviados (nome completo no title) para sobrar espaco para a observacao. --%>
                            <th data-sort-desc title="Data e hora">Data</th>
                            <th title="Ponto focal">P.focal</th>
                            <th title="Prioridade">Prior.</th>
                            <th>Assunto</th>
                            <th title="Serviço e prestador">Serv.</th>
                            <th>Status</th>
                            <th style="min-width: 320px;" title="Observação">Obs.</th>
                            <c:if test="${!readOnly}"><th data-no-sort></th></c:if>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="a" items="${agenda}">
                            <tr class="clickable-row" data-id="${a.id}"
                                onclick="window.location='${pageContext.request.contextPath}/agenda/form?id=${a.id}'">
                                <%-- Hora abaixo da data; o espaco antes do <br> mantem o texto "dd/mm/aa HH:mm",
                                     que o dataTable.js ordena e filtra como data e hora. --%>
                                <td style="white-space:nowrap;">${a.dataFormatada}<c:if test="${not empty a.horaFormatada}"> <br>${a.horaFormatada}</c:if></td>
                                <td>${a.pontoFocal}</td>
                                <td><span class="badge badge-prioridade-${a.prioridade.dbValue}">${a.prioridade.label}</span></td>
                                <td>${a.assunto}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty a.servicoDescricao}">-</c:when>
                                        <c:otherwise>
                                            ${a.servicoDescricao}
                                            <c:if test="${not empty a.prestadorNome}"><br>${a.prestadorNome}</c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge badge-${a.status.dbValue}">${a.status.label}</span>
                                    <c:if test="${not empty a.dataStatus}"><br>${a.dataStatusFormatada}</c:if>
                                </td>
                                <td>${empty a.observacao ? '-' : a.observacao}</td>
                                <c:if test="${!readOnly}">
                                    <td onclick="event.stopPropagation();">
                                        <form method="post" action="${pageContext.request.contextPath}/agenda/excluir"
                                              onsubmit="return confirm('Excluir este compromisso?');">
                                            <input type="hidden" name="id" value="${a.id}">
                                            <button type="submit" class="menu-icon-link" title="Excluir compromisso" aria-label="Excluir compromisso">🗑️</button>
                                        </form>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<dialog id="agendaPdfDialog" class="agenda-pdf-dialog">
    <form id="agendaPdfForm" method="get" action="${pageContext.request.contextPath}/agenda/pdf"
          onsubmit="return prepararPdf()">
        <h2 class="title" style="text-align:left;margin:0 0 4px;font-size:18px;">Gerar PDF</h2>
        <p class="subtitle" style="text-align:left;margin:0;">Somente os compromissos visíveis com os filtros atuais.</p>
        <div class="field" style="margin-top:12px;">
            <label>Ordenar por</label>
            <div class="radio-row">
                <label class="radio-option">
                    <input type="radio" name="ordem" value="data" checked>
                    <span>Data</span>
                </label>
                <label class="radio-option">
                    <input type="radio" name="ordem" value="pontoFocal">
                    <span>Ponto focal</span>
                </label>
            </div>
        </div>
        <input type="hidden" name="ids" disabled>
        <input type="hidden" name="excluir" disabled>
        <div class="agenda-pdf-actions">
            <button type="button" class="btn btn-secondary" onclick="document.getElementById('agendaPdfDialog').close()">Cancelar</button>
            <button type="submit" class="btn btn-primary">Gerar PDF</button>
        </div>
    </form>
</dialog>

<script>
    function abrirDialogoPdf() {
        document.getElementById('agendaPdfDialog').showModal();
    }

    /* Manda a menor lista: so os ids visiveis ("ids") ou so os escondidos pelo filtro ("excluir"). */
    function prepararPdf() {
        var visiveis = [];
        var escondidos = [];
        document.querySelectorAll('table.data-table tbody tr[data-id]').forEach(function (row) {
            (row.style.display === 'none' ? escondidos : visiveis).push(row.getAttribute('data-id'));
        });
        if (!visiveis.length) {
            alert('Nenhum compromisso visível com os filtros atuais.');
            return false;
        }
        var form = document.getElementById('agendaPdfForm');
        var ids = form.querySelector('input[name="ids"]');
        var excluir = form.querySelector('input[name="excluir"]');
        var usarIds = visiveis.length <= escondidos.length;
        ids.disabled = !usarIds;
        ids.value = usarIds ? visiveis.join(',') : '';
        excluir.disabled = usarIds || !escondidos.length;
        excluir.value = usarIds ? '' : escondidos.join(',');
        document.getElementById('agendaPdfDialog').close();
        return true;
    }
</script>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=7"></script>
</body>
</html>
