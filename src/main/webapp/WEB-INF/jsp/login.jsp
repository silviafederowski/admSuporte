<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Entrar - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card">
        <img class="logo" src="${pageContext.request.contextPath}/logo" alt="Logotipo">
        <h1 class="title">Bem-vindo de volta</h1>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">
            <div class="field">
                <label for="identifier">E-mail ou telefone</label>
                <input type="text" id="identifier" name="identifier" placeholder="você@exemplo.com"
                       value="${identifier}" autocomplete="off" required>
            </div>
            <div class="field">
                <label for="password">Senha</label>
                <input type="password" id="password" name="password" placeholder="Sua senha"
                       autocomplete="new-password" required>
            </div>

            <div class="forgot-link">
                <a class="link" href="${pageContext.request.contextPath}/forgot-password">Esqueci minha senha</a>
            </div>

            <button type="submit" class="btn btn-primary">Entrar</button>
        </form>
    </div>
</div>
</body>
</html>
