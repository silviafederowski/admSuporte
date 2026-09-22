<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "tipos-usuario") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tipo de usuário - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=30">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${empty param.id}">Novo tipo de usuário</c:when>
                    <c:otherwise>Editar tipo de usuário</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="tipoUsuarioForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/tipos-usuario" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="tipoUsuarioExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="tipoUsuarioForm" method="post" action="${pageContext.request.contextPath}/tipos-usuario/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row">
                <div class="field">
                    <label for="name">Nome</label>
                    <input type="text" id="name" name="name" value="${tipo.name}" ${dis} required>
                    <c:if test="${not empty errors.name}"><div class="field-error">${errors.name}</div></c:if>
                </div>
                <div class="field">
                    <label for="nivel">Nível</label>
                    <input type="text" id="nivel" name="nivel" inputmode="numeric" pattern="[0-9]{2,}"
                           maxlength="3" placeholder="00"
                           value="${not empty param.nivel ? param.nivel : tipo.nivelFormatado}" ${dis} required>
                    <c:if test="${empty errors.nivel}">
                        <div class="field-hint">Níveis menores que 10 precisam do zero à esquerda (ex.: 05).</div>
                    </c:if>
                    <c:if test="${not empty errors.nivel}"><div class="field-error">${errors.nivel}</div></c:if>
                </div>
            </div>

        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="tipoUsuarioExcluirForm" method="post" action="${pageContext.request.contextPath}/tipos-usuario/excluir"
                  onsubmit="return confirm('Excluir este tipo de usuário? Isso só é possível se não houver usuários com esse tipo.');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
