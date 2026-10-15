<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Histórico / Pendências de serviços - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=33">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Histórico / Pendências de serviços</h1>
            </div>
            <div class="toolbar-actions">
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/servicos" title="Voltar para serviços" aria-label="Voltar para serviços">↩️</a>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/pendencias-servico" class="filtro-form">
            <div class="field">
                <label for="filtroServico">Serviço</label>
                <select id="filtroServico" name="servicoId" onchange="this.form.submit()">
                    <option value="">Todos</option>
                    <c:forEach var="s" items="${servicos}">
                        <option value="${s.id}" ${servicoIdFiltro == s.id ? 'selected' : ''}>${s.descricao}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="filtroPontoFocal">Ponto focal</label>
                <select id="filtroPontoFocal" name="pontoFocal" onchange="this.form.submit()">
                    <option value="">Todos</option>
                    <c:forEach var="pf" items="${pontosFocais}">
                        <option value="${pf}" ${pontoFocalFiltro == pf ? 'selected' : ''}>${pf}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="field">
                <label for="filtroStatus">Situação</label>
                <select id="filtroStatus" name="status" onchange="this.form.submit()">
                    <option value="">Todas</option>
                    <option value="pendente" ${statusFiltro == 'pendente' ? 'selected' : ''}>Pendente</option>
                    <option value="resolvido" ${statusFiltro == 'resolvido' ? 'selected' : ''}>Resolvido</option>
                </select>
            </div>
            <c:if test="${filtroAtivo}">
                <a class="btn btn-secondary btn-small" href="${pageContext.request.contextPath}/pendencias-servico">Limpar filtros</a>
            </c:if>
        </form>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty registros}">
                <p class="empty-state">${filtroAtivo ? 'Nenhum registro encontrado com esses filtros.' : 'Nenhum registro no histórico ainda.'}</p>
            </c:when>
            <c:otherwise>
                <%-- Compromissos da agenda ligados a servico; ja vem do servidor em ordem decrescente de data. --%>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th data-sort-desc style="width:90px;">Data</th>
                            <c:if test="${empty servicoIdFiltro}"><th>Serviço</th></c:if>
                            <th>Prestador</th>
                            <th style="text-align:left;">Descrição</th>
                            <th style="width:110px;" title="Ponto focal">P. focal</th>
                            <th style="width:120px;">Situação</th>
                            <th style="width:110px;">Data resolução</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="a" items="${registros}">
                            <tr class="clickable-row" title="Abrir compromisso na agenda"
                                onclick="window.location='${pageContext.request.contextPath}/agenda/form?id=${a.id}'">
                                <td>${a.dataFormatada}</td>
                                <c:if test="${empty servicoIdFiltro}"><td>${a.servicoDescricao}</td></c:if>
                                <td>${empty a.prestadorNome ? '-' : a.prestadorNome}</td>
                                <td style="text-align:left;">${a.assunto}</td>
                                <td>${empty a.pontoFocal ? '-' : a.pontoFocal}</td>
                                <td><span class="badge badge-${a.status.dbValue}">${a.status.label}</span></td>
                                <td>${a.status.dbValue == 'resolvido' && not empty a.dataStatus ? a.dataStatusFormatada : '-'}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=7"></script>
</body>
</html>
