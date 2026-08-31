<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Criar conta - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <h1 class="title">Criar conta</h1>
        <p class="subtitle">Preencha seus dados para comecar</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="field">
                <label for="name">Nome</label>
                <input type="text" id="name" name="name" placeholder="Seu nome" value="${name}" required>
                <c:if test="${not empty errors.name}"><div class="field-error">${errors.name}</div></c:if>
            </div>
            <div class="field">
                <label for="email">E-mail</label>
                <input type="email" id="email" name="email" placeholder="voce@exemplo.com" value="${email}" required>
                <c:if test="${not empty errors.email}"><div class="field-error">${errors.email}</div></c:if>
            </div>
            <div class="field">
                <label for="phone">Telefone</label>
                <input type="tel" id="phone" name="phone" placeholder="(11) 91234-5678" value="${phone}" required>
                <c:if test="${not empty errors.phone}"><div class="field-error">${errors.phone}</div></c:if>
            </div>
            <div class="field">
                <label for="password">Senha</label>
                <input type="password" id="password" name="password" placeholder="Minimo 6 caracteres" required>
                <c:if test="${not empty errors.password}"><div class="field-error">${errors.password}</div></c:if>
            </div>
            <div class="field">
                <label for="confirmPassword">Confirmar senha</label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Repita a senha" required>
                <c:if test="${not empty errors.confirmPassword}"><div class="field-error">${errors.confirmPassword}</div></c:if>
            </div>
            <div class="field">
                <label for="userTypeId">Tipo de usuario</label>
                <select id="userTypeId" name="userTypeId" required>
                    <option value="" disabled ${empty userTypeId ? 'selected' : ''}>Selecione...</option>
                    <c:forEach var="type" items="${userTypes}">
                        <option value="${type.id}" ${userTypeId == type.id ? 'selected' : ''}>${type.name}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.userTypeId}"><div class="field-error">${errors.userTypeId}</div></c:if>
            </div>

            <button type="submit" class="btn btn-primary">Criar conta</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar para o menu</a>
        </form>
    </div>
</div>
</body>
</html>
