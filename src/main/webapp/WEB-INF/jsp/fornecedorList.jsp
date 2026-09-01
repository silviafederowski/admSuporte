<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fornecedores - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de fornecedores</h1>
            </div>
            <c:if test="${sessionScope.userNivel <= 9}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/fornecedores/form">Novo fornecedor</a>
            </c:if>
        </div>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty fornecedores}">
                <p class="empty-state">Nenhum fornecedor cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Nome / Razão social</th>
                            <th>Email</th>
                            <th>Contatos</th>
                            <th>Classificação</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="f" items="${fornecedores}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/fornecedores/form?id=${f.id}'">
                                <td>${f.nomeRazaoSocial}</td>
                                <td>${f.email}</td>
                                <td>
                                    <c:if test="${not empty f.contato1Nome}">${f.contato1Nome} (${f.contato1Cargo}) - ${f.contato1Telefone}<br></c:if>
                                    <c:if test="${not empty f.contato2Nome}">${f.contato2Nome} (${f.contato2Cargo}) - ${f.contato2Telefone}<br></c:if>
                                    <c:if test="${not empty f.contato3Nome}">${f.contato3Nome} (${f.contato3Cargo}) - ${f.contato3Telefone}</c:if>
                                </td>
                                <td><span class="badge badge-${f.classificacao.dbValue}">${f.classificacao.label}</span></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js"></script>
</body>
</html>
