<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Histórico de serviços - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Histórico de serviços</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <c:url var="novoRegistroUrl" value="/pendencias-servico/form">
                        <c:if test="${not empty servicoIdFiltro}">
                            <c:param name="servicoId" value="${servicoIdFiltro}" />
                        </c:if>
                    </c:url>
                    <a class="btn btn-primary" href="${novoRegistroUrl}">Novo registro</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty servicoFiltro}">
            <p class="form-info">Mostrando o histórico do serviço "${servicoFiltro.descricao}".
                <a href="${pageContext.request.contextPath}/pendencias-servico">Ver todo o histórico</a>
            </p>
        </c:if>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty pendencias}">
                <p class="empty-state">Nenhum registro no histórico ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Serviço</th>
                            <th>Prestador</th>
                            <th>Data</th>
                            <th>Técnico</th>
                            <th style="min-width: 320px;">Descrição da ocorrência</th>
                            <th>Situação</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${pendencias}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/pendencias-servico/form?id=${p.id}'">
                                <td>${p.servicoDescricao}</td>
                                <td>${p.prestadorNome}</td>
                                <td>${p.dataFormatada}</td>
                                <td>${p.nomeTecnico}</td>
                                <td>${p.descricaoOcorrencia}</td>
                                <td><span class="badge badge-${p.status.dbValue}">${p.status.label}</span></td>
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
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
</body>
</html>
