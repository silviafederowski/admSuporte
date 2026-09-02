<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="readOnly" value="${sessionScope.userNivel > 9}" />
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<c:set var="isSelf" value="${not empty param.id && usuario.id == sessionScope.userId}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Usuário - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar usuário</c:when>
                    <c:when test="${empty param.id}">Novo usuário</c:when>
                    <c:otherwise>Editar usuário</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">Dados do usuário</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/usuarios/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="name">Nome</label>
                <input type="text" id="name" name="name" value="${usuario.name}" ${dis} required>
                <c:if test="${not empty errors.name}"><div class="field-error">${errors.name}</div></c:if>
            </div>
            <div class="field">
                <label for="email">E-mail</label>
                <input type="email" id="email" name="email" placeholder="você@exemplo.com" value="${usuario.email}" ${dis} required>
                <c:if test="${not empty errors.email}"><div class="field-error">${errors.email}</div></c:if>
            </div>
            <div class="field">
                <label for="phone">Telefone</label>
                <input type="tel" id="phone" name="phone" placeholder="(11) 91234-5678" value="${usuario.phone}" ${dis} required>
                <c:if test="${not empty errors.phone}"><div class="field-error">${errors.phone}</div></c:if>
            </div>
            <div class="field">
                <label for="password">${empty param.id ? 'Senha' : 'Nova senha (deixe em branco para não alterar)'}</label>
                <input type="password" id="password" name="password" placeholder="Mínimo 6 caracteres" ${dis}>
                <c:if test="${not empty errors.password}"><div class="field-error">${errors.password}</div></c:if>
            </div>
            <div class="field">
                <label for="confirmPassword">Confirmar senha</label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Repita a senha" ${dis}>
                <c:if test="${not empty errors.confirmPassword}"><div class="field-error">${errors.confirmPassword}</div></c:if>
            </div>
            <div class="field">
                <label for="userTypeId">Tipo de usuário</label>
                <c:set var="selectedUserTypeId" value="${not empty param.userTypeId ? param.userTypeId : usuario.userTypeId}" />
                <select id="userTypeId" name="userTypeId" ${dis} required>
                    <option value="" disabled ${empty selectedUserTypeId || selectedUserTypeId == 0 ? 'selected' : ''}>Selecione...</option>
                    <c:forEach var="type" items="${userTypes}">
                        <option value="${type.id}" ${selectedUserTypeId == type.id ? 'selected' : ''}>${type.name}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.userTypeId}"><div class="field-error">${errors.userTypeId}</div></c:if>
            </div>
            <div class="field">
                <c:choose>
                    <c:when test="${isSelf}">
                        <label class="row-item" style="padding-left:0;">
                            <input type="checkbox" checked disabled>
                            <span>Ativo</span>
                        </label>
                        <input type="hidden" name="ativo" value="on">
                        <div class="field-error" style="color:var(--text-muted);">Não é possível desativar o próprio usuário.</div>
                    </c:when>
                    <c:otherwise>
                        <label class="row-item" style="padding-left:0;">
                            <input type="checkbox" name="ativo" ${usuario.ativo ? 'checked' : ''} ${dis}>
                            <span>Ativo</span>
                        </label>
                    </c:otherwise>
                </c:choose>
                <c:if test="${not empty errors.ativo}"><div class="field-error">${errors.ativo}</div></c:if>
            </div>

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/usuarios">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>
    </div>
</div>
</body>
</html>
