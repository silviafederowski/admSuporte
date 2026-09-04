<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Menu - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide menu-page">
        <img class="logo" src="${pageContext.request.contextPath}/logo" alt="Logotipo">
        <h1 class="title">Bem-vindo, <span style="text-transform:capitalize;">${sessionScope.userType}</span> ${sessionScope.userName}!</h1>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>

        <div class="menu-grid">
            <c:if test="${telasPermitidas.contains('documentos')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/procedimentos">Documentos</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('vagas')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/vagas">Vagas</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('veiculos')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/veiculos">Veículos</a>
            </c:if>

            <c:if test="${telasPermitidas.contains('servicos')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos">Serviços</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('pendencias-servico')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/pendencias-servico">Histórico de serviços</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('produtos')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/produtos">Produtos</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('prestadores')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/prestadores">Prestadores</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('fornecedores')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/fornecedores">Fornecedores</a>
            </c:if>

            <c:if test="${telasPermitidas.contains('usuarios')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/usuarios">Usuários</a>
            </c:if>

            <c:if test="${telasPermitidas.contains('operacoes')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/operacoes">Log de operações</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('google-form')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/google-form">Formulário Google</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('documento-condominio')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/documento-condominio">Dados do condomínio</a>
            </c:if>
            <c:if test="${telasPermitidas.contains('tipos-usuario')}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/tipos-usuario">Tipos de usuário</a>
            </c:if>

            <!-- Autorizacoes: consulta liberada a qualquer usuario logado, edicao so ao sindico (nivel 0) -->
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/autorizacoes">Autorizações</a>
        </div>

        <a class="btn btn-secondary menu-logout" href="${pageContext.request.contextPath}/logout">Sair</a>
    </div>
</div>
</body>
</html>
