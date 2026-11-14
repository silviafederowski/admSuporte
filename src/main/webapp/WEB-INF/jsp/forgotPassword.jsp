<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Esqueci minha senha - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=48">
</head>
<body>
<div class="page">
    <div class="card">
        <p class="condominio-nome">${nomeCondominio}</p>
        <h1 class="title">Esqueci minha senha</h1>
        <p class="subtitle">
            Peça a um administrador do condomínio para trocar a sua senha.
            Ele define uma nova senha para você na tela de Usuários.
        </p>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/login">Voltar para o login</a>
    </div>
</div>
</body>
</html>
