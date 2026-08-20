<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Menu - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card menu-page">
        <img class="logo" src="${pageContext.request.contextPath}/logo" alt="Logotipo">
        <h1 class="title">Bem-vindo, ${sessionScope.userName}!</h1>
        <p class="subtitle">O que voce quer fazer?</p>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/google-form">Formulario Google</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/placeholder?title=Outra">Outra</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/logout">Sair</a>
    </div>
</div>
</body>
</html>
