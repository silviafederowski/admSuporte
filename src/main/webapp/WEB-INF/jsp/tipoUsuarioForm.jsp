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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${empty param.id}">Novo tipo de usuário</c:when>
                    <c:otherwise>Editar tipo de usuário</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/tipos-usuario/form">
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

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/tipos-usuario">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form method="post" action="${pageContext.request.contextPath}/tipos-usuario/excluir"
                  onsubmit="return confirm('Excluir este tipo de usuário? Isso só é possível se não houver usuários com esse tipo.');">
                <input type="hidden" name="id" value="${param.id}">
                <button type="submit" class="btn btn-danger">Excluir</button>
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
