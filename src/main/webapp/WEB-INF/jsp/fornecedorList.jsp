<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fornecedores - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=9">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de fornecedores</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/fornecedores/form" title="Novo fornecedor" aria-label="Novo fornecedor">➕</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/fornecedores" class="filter-bar">
            <label for="produtoIdFiltro" title="Filtrar por produto"><span class="filter-icon" aria-hidden="true"></span> por produto</label>
            <select id="produtoIdFiltro" name="produtoId" onchange="this.form.submit()">
                <option value="" ${empty produtoIdFiltro ? 'selected' : ''}>Todos os produtos</option>
                <c:forEach var="p" items="${produtosFiltro}">
                    <option value="${p.id}" ${produtoIdFiltro == p.id ? 'selected' : ''}>${p.descricao}</option>
                </c:forEach>
            </select>
            <c:if test="${not empty produtoIdFiltro}">
                <a class="link" href="${pageContext.request.contextPath}/fornecedores">Limpar filtro</a>
            </c:if>
        </form>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty fornecedores}">
                <p class="empty-state">
                    <c:choose>
                        <c:when test="${not empty produtoIdFiltro}">Nenhum fornecedor fornece esse produto.</c:when>
                        <c:otherwise>Nenhum fornecedor cadastrado ainda.</c:otherwise>
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
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="f" items="${fornecedores}">
                            <c:set var="temContato2" value="${not empty f.contato2Nome}" />
                            <c:set var="temContato3" value="${not empty f.contato3Nome}" />
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/fornecedores/form?id=${f.id}'">
                                <td style="text-align:left;">${f.nomeRazaoSocial}</td>
                                <td style="text-align:left;"><c:if test="${not empty f.contato1Nome}">${f.contato1Nome} (${f.contato1Cargo})</c:if></td>
                                <td>${f.contato1Telefone}</td>
                                <td><span class="badge badge-${f.classificacao.dbValue}">${f.classificacao.label}</span></td>
                            </tr>
                            <c:if test="${temContato2}">
                                <tr class="clickable-row contact-row-extra"
                                    onclick="window.location='${pageContext.request.contextPath}/fornecedores/form?id=${f.id}'">
                                    <td style="text-align:left;"><span class="repeat-hidden">${f.nomeRazaoSocial}</span></td>
                                    <td style="text-align:left;">${f.contato2Nome} (${f.contato2Cargo})</td>
                                    <td>${f.contato2Telefone}</td>
                                    <td><span class="repeat-hidden badge badge-${f.classificacao.dbValue}">${f.classificacao.label}</span></td>
                                </tr>
                            </c:if>
                            <c:if test="${temContato3}">
                                <tr class="clickable-row contact-row-extra"
                                    onclick="window.location='${pageContext.request.contextPath}/fornecedores/form?id=${f.id}'">
                                    <td style="text-align:left;"><span class="repeat-hidden">${f.nomeRazaoSocial}</span></td>
                                    <td style="text-align:left;">${f.contato3Nome} (${f.contato3Cargo})</td>
                                    <td>${f.contato3Telefone}</td>
                                    <td><span class="repeat-hidden badge badge-${f.classificacao.dbValue}">${f.classificacao.label}</span></td>
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
