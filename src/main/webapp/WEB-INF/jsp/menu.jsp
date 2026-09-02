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
        <p class="subtitle">O que você quer fazer?</p>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/procedimentos">Documentos</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/vagas">Vagas</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/veiculos">Veículos</a>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos">Serviços</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/produtos">Produtos</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/prestadores">Prestadores</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/fornecedores">Fornecedores</a>

        <c:if test="${sessionScope.userNivel <= 10}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/usuarios">Usuários</a>
        </c:if>

        <c:if test="${sessionScope.userNivel <= 9}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/operacoes">Log de operações</a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/google-form">Formulário Google</a>
        </c:if>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/logout">Sair</a>
    </div>
</div>
</body>
</html>
