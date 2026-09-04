<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Documentos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Documentos</h1>
            </div>
            <div class="toolbar-actions">
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>
        <p class="subtitle">Clique em um arquivo para consultá-lo.</p>

        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty arquivos}">
                <p class="empty-state">Nenhum arquivo encontrado nesta pasta.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th style="text-align:left;">Nome</th>
                            <th>Tipo</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="a" items="${arquivos}">
                            <c:url var="verUrl" value="/procedimentos/ver">
                                <c:param name="id" value="${a.id}" />
                                <c:param name="nome" value="${a.nome}" />
                                <c:param name="mimeType" value="${a.mimeType}" />
                            </c:url>
                            <tr class="clickable-row" onclick="window.location='${verUrl}'">
                                <td style="text-align:left;">${a.nome}</td>
                                <td>${a.tipo}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty arquivosProcedimentos}">
            <h2 class="subtitle" style="margin-top:24px;">Procedimentos</h2>
            <div class="table-wrap">
                <table class="data-table" data-sortable>
                    <thead>
                    <tr>
                        <th style="text-align:left;">Nome</th>
                        <th>Tipo</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="ap" items="${arquivosProcedimentos}">
                        <c:url var="verUrlProcedimento" value="/procedimentos/ver">
                            <c:param name="id" value="${ap.id}" />
                            <c:param name="nome" value="${ap.nome}" />
                            <c:param name="mimeType" value="${ap.mimeType}" />
                        </c:url>
                        <tr class="clickable-row" onclick="window.location='${verUrlProcedimento}'">
                            <td style="text-align:left;">${ap.nome}</td>
                            <td>${ap.tipo}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
</body>
</html>
