<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Prestadores - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de prestadores</h1>
            </div>
            <c:if test="${sessionScope.userNivel <= 9}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/prestadores/form">Novo prestador</a>
            </c:if>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/prestadores" class="filter-bar">
            <label for="servicoIdFiltro">Filtrar por serviço</label>
            <select id="servicoIdFiltro" name="servicoId" onchange="this.form.submit()">
                <option value="" ${empty servicoIdFiltro ? 'selected' : ''}>Todos os serviços</option>
                <c:forEach var="s" items="${servicosFiltro}">
                    <option value="${s.id}" ${servicoIdFiltro == s.id ? 'selected' : ''}>${s.descricao}</option>
                </c:forEach>
            </select>
            <c:if test="${not empty servicoIdFiltro}">
                <a class="link" href="${pageContext.request.contextPath}/prestadores">Limpar filtro</a>
            </c:if>
        </form>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty prestadores}">
                <p class="empty-state">
                    <c:choose>
                        <c:when test="${not empty servicoIdFiltro}">Nenhum prestador presta esse serviço.</c:when>
                        <c:otherwise>Nenhum prestador cadastrado ainda.</c:otherwise>
                    </c:choose>
                </p>
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
                            <th>Regular/Contratado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${prestadores}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/prestadores/form?id=${p.id}'">
                                <td>${p.nomeRazaoSocial}</td>
                                <td>${p.email}</td>
                                <td>
                                    <c:if test="${not empty p.contato1Nome}">${p.contato1Nome} (${p.contato1Cargo}) - ${p.contato1Telefone}<br></c:if>
                                    <c:if test="${not empty p.contato2Nome}">${p.contato2Nome} (${p.contato2Cargo}) - ${p.contato2Telefone}<br></c:if>
                                    <c:if test="${not empty p.contato3Nome}">${p.contato3Nome} (${p.contato3Cargo}) - ${p.contato3Telefone}</c:if>
                                </td>
                                <td><span class="badge badge-${p.classificacao.dbValue}">${p.classificacao.label}</span></td>
                                <td>${p.regularOuContratado.label}</td>
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
