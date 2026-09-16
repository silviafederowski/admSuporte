<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "tipos-produtos") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tipo de produto - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=11">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${empty param.id}">Novo tipo de produto</c:when>
                    <c:otherwise>Editar tipo de produto</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="tipoProdutoForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/tipos-produtos" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${not empty param.id}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/tipos-produtos/lista-compras?id=${param.id}" title="Lista de compras" aria-label="Lista de compras">🛒</a>
                </c:if>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="tipoProdutoExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="tipoProdutoForm" method="post" action="${pageContext.request.contextPath}/tipos-produtos/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="descricao">Descrição</label>
                <input type="text" id="descricao" name="descricao" value="${tipo.descricao}" ${dis} required>
                <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
            </div>

        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="tipoProdutoExcluirForm" method="post" action="${pageContext.request.contextPath}/tipos-produtos/excluir"
                  onsubmit="return confirm('Excluir este tipo de produto? Isso só é possível se não houver produtos desse tipo.');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
