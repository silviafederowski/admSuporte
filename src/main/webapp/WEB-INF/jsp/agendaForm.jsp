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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=9">
</head>
<body>
<div class="page">
    <div class="card card-wide">
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
                    <input type="date" id="data" name="data" value="${agendaItem.data}" ${dis} required>
                    <c:if test="${not empty errors.data}"><div class="field-error">${errors.data}</div></c:if>
                </div>
                <div class="field">
                    <label for="hora">Hora</label>
                    <input type="time" id="hora" name="hora" value="${agendaItem.hora}" ${dis} required>
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
</body>
</html>
