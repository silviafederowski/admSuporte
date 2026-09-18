<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Prestadores - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=19">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de prestadores</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/prestadores/form" title="Novo prestador" aria-label="Novo prestador">➕</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/prestadores" class="filter-bar">
            <label for="servicoIdFiltro" title="Filtrar por serviço"><span class="filter-icon" aria-hidden="true"></span> por serviço</label>
            <select id="servicoIdFiltro" name="servicoId" onchange="this.form.submit()">
                <option value="" ${empty servicoIdFiltro ? 'selected' : ''}>Todos os serviços</option>
                <c:forEach var="s" items="${servicosFiltro}">
                    <option value="${s.id}" ${servicoIdFiltro == s.id ? 'selected' : ''}>${s.descricao}</option>
                </c:forEach>
            </select>
            <c:if test="${not empty servicoIdFiltro}">
                <a class="link" href="${pageContext.request.contextPath}/prestadores">Limpar filtro</a>
            </c:if>
        </form>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty prestadores}">
                <p class="empty-state">
                    <c:choose>
                        <c:when test="${not empty servicoIdFiltro}">Nenhum prestador presta esse serviço.</c:when>
                        <c:otherwise>Nenhum prestador cadastrado ainda.</c:otherwise>
                    </c:choose>
                </p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th style="text-align:left;">Nome / Razão social</th>
                            <th style="text-align:left;">Contatos</th>
                            <th>Telefones</th>
                            <th>Classificação</th>
                            <th>Tipo</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${prestadores}">
                            <c:set var="temContato2" value="${not empty p.contato2Nome}" />
                            <c:set var="temContato3" value="${not empty p.contato3Nome}" />
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/prestadores/form?id=${p.id}'">
                                <td style="text-align:left;">${p.nomeRazaoSocial}</td>
                                <td style="text-align:left;"><c:if test="${not empty p.contato1Nome}">${p.contato1Nome} (${p.contato1Cargo})</c:if></td>
                                <td>${p.contato1Telefone}</td>
                                <td><span class="badge badge-${p.classificacao.dbValue}">${p.classificacao.label}</span></td>
                                <td>${p.tipo.label}</td>
                            </tr>
                            <c:if test="${temContato2}">
                                <tr class="clickable-row contact-row-extra"
                                    onclick="window.location='${pageContext.request.contextPath}/prestadores/form?id=${p.id}'">
                                    <td style="text-align:left;"><span class="repeat-hidden">${p.nomeRazaoSocial}</span></td>
                                    <td style="text-align:left;">${p.contato2Nome} (${p.contato2Cargo})</td>
                                    <td>${p.contato2Telefone}</td>
                                    <td><span class="repeat-hidden badge badge-${p.classificacao.dbValue}">${p.classificacao.label}</span></td>
                                    <td><span class="repeat-hidden">${p.tipo.label}</span></td>
                                </tr>
                            </c:if>
                            <c:if test="${temContato3}">
                                <tr class="clickable-row contact-row-extra"
                                    onclick="window.location='${pageContext.request.contextPath}/prestadores/form?id=${p.id}'">
                                    <td style="text-align:left;"><span class="repeat-hidden">${p.nomeRazaoSocial}</span></td>
                                    <td style="text-align:left;">${p.contato3Nome} (${p.contato3Cargo})</td>
                                    <td>${p.contato3Telefone}</td>
                                    <td><span class="repeat-hidden badge badge-${p.classificacao.dbValue}">${p.classificacao.label}</span></td>
                                    <td><span class="repeat-hidden">${p.tipo.label}</span></td>
                                </tr>
                            </c:if>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
</body>
</html>
