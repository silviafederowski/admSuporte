<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Criar nova senha - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=48">
</head>
<body>
<div class="page">
    <div class="card">
        <p class="condominio-nome">${nomeCondominio}</p>
        <h1 class="title">Crie a sua senha</h1>
        <p class="subtitle">
            Olá, <c:out value="${sessionScope.userName}" />. A sua senha atual foi definida por um administrador.
            Para continuar, crie uma senha só sua.
        </p>

        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/trocar-senha" autocomplete="off">
            <div class="field">
                <label for="password">Nova senha</label>
                <input type="password" id="password" name="password" placeholder="Mínimo 6 caracteres"
                       autocomplete="new-password" required>
            </div>
            <div class="field">
                <label for="confirmPassword">Confirmar nova senha</label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Repita a nova senha"
                       autocomplete="new-password" required>
            </div>

            <button type="submit" class="btn btn-primary">Salvar e entrar</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/logout">Sair</a>
        </form>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/senhaVisivel.js?v=6"></script>
</body>
</html>
