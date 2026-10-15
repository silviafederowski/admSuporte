<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Histórico / Pendências de serviços - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=32">
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
                <c:if test="${!readOnly}">
                    <c:url var="novoRegistroUrl" value="/pendencias-servico/form">
                        <c:if test="${not empty servicoIdFiltro}">
                            <c:param name="servicoId" value="${servicoIdFiltro}" />
                        </c:if>
                    </c:url>
                    <a class="menu-icon-link" href="${novoRegistroUrl}" title="Novo registro" aria-label="Novo registro">➕</a>
                </c:if>
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
            <c:when test="${empty pendencias}">
                <p class="empty-state">${filtroAtivo ? 'Nenhum registro encontrado com esses filtros.' : 'Nenhum registro no histórico ainda.'}</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="p" items="${pendencias}">
                    <div class="pendencia-bloco">
                        <a class="pendencia-cabecalho"
                           href="${pageContext.request.contextPath}/pendencias-servico/form?id=${p.id}">
                            <strong>${p.dataFormatada}</strong> — ${p.servicoDescricao}
                            <c:if test="${not empty p.prestadorNome || not empty p.nomeTecnico}">
                                <span class="pendencia-detalhe">
                                    <c:if test="${not empty p.prestadorNome}">Prestador: ${p.prestadorNome}</c:if>
                                    <c:if test="${not empty p.prestadorNome && not empty p.nomeTecnico}"> · </c:if>
                                    <c:if test="${not empty p.nomeTecnico}">Técnico: ${p.nomeTecnico}</c:if>
                                </span>
                            </c:if>
                        </a>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th style="text-align:left;">Descrição</th>
                                    <th style="width:110px;" title="Ponto focal">P. focal</th>
                                    <th style="width:140px;">Situação</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${p.itens}">
                                    <tr>
                                        <td style="text-align:left;">${item.descricao}</td>
                                        <td>${item.pontoFocal}</td>
                                        <td><span class="badge badge-${item.status.dbValue}">${item.status.label}</span></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>
