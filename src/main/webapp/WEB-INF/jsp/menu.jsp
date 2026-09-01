<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>

        <c:if test="${sessionScope.userNivel <= 9}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/google-form">Formulario Google</a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/register">Criar conta</a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/operacoes">Log de operacoes</a>
        </c:if>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/prestadores">Cadastro de prestadores</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos">Servicos de manutencao</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/fornecedores">Cadastro de fornecedores</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/produtos">Cadastro de produtos</a>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/logout">Sair</a>
    </div>
</div>
</body>
</html>
