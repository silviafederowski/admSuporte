<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Agenda - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=9">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Agenda</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/agenda/form" title="Novo compromisso" aria-label="Novo compromisso">➕</a>
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
                            <th>Data</th>
                            <th>Hora</th>
                            <th>Ponto focal</th>
                            <th>Assunto</th>
                            <th style="min-width: 320px;">Observação</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="a" items="${agenda}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/agenda/form?id=${a.id}'">
                                <td>${a.dataFormatada}</td>
                                <td>${a.horaFormatada}</td>
                                <td>${a.pontoFocal}</td>
                                <td>${a.assunto}</td>
                                <td>${empty a.observacao ? '-' : a.observacao}</td>
                            </tr>
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
