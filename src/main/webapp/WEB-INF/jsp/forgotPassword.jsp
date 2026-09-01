<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Recuperar senha - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <h1 class="title">Esqueci minha senha</h1>
        <p class="subtitle">Escolha como deseja receber o código de verificação</p>

        <c:set var="currentMethod" value="${empty method ? 'email' : method}" />

        <div class="segment-row">
            <a class="segment ${currentMethod == 'email' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/forgot-password?method=email">E-mail</a>
            <a class="segment ${currentMethod == 'sms' ? 'active' : ''}"
               href="${pageContext.request.contextPath}/forgot-password?method=sms">SMS</a>
        </div>

        <c:if test="${not empty error}">
            <p class="form-error">${error}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/forgot-password">
            <input type="hidden" name="method" value="${currentMethod}">
            <div class="field">
                <c:choose>
                    <c:when test="${currentMethod == 'sms'}">
                        <label for="destination">Telefone</label>
                        <input type="tel" id="destination" name="destination" placeholder="(11) 91234-5678" value="${destination}" required>
                    </c:when>
                    <c:otherwise>
                        <label for="destination">E-mail</label>
                        <input type="email" id="destination" name="destination" placeholder="você@exemplo.com" value="${destination}" required>
                    </c:otherwise>
                </c:choose>
            </div>

            <button type="submit" class="btn btn-primary">Enviar código</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/login">Voltar para o login</a>
        </form>
    </div>
</div>
</body>
</html>
