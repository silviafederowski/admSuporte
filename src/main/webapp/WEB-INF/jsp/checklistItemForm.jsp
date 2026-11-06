<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "checklist-itens") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Item de checklist - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=34">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar item de checklist</c:when>
                    <c:when test="${empty param.id}">Novo item de checklist</c:when>
                    <c:otherwise>Editar item de checklist</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="checklistItemForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/checklist-itens" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="checklistItemExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="checklistItemForm" method="post" action="${pageContext.request.contextPath}/checklist-itens/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row">
                <div class="field">
                    <label for="userTypeId">Tipo usuário</label>
                    <select id="userTypeId" name="userTypeId" ${dis} required>
                        <option value="" ${item.userTypeId == 0 ? 'selected' : ''}>Selecione...</option>
                        <c:forEach var="t" items="${userTypes}">
                            <option value="${t.id}" ${item.userTypeId == t.id ? 'selected' : ''}>${t.name}</option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.userTypeId}"><div class="field-error">${errors.userTypeId}</div></c:if>
                </div>
            </div>

            <div class="field">
                <label for="descricao">Descrição</label>
                <input type="text" id="descricao" name="descricao" value="${item.descricao}" maxlength="255" ${dis} required>
                <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
            </div>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="checklistItemExcluirForm" method="post" action="${pageContext.request.contextPath}/checklist-itens/excluir"
                  onsubmit="return confirm('Excluir este item? Isso só é possível se ele ainda não foi usado em nenhum checklist.');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
