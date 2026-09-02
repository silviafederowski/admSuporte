<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Produtos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de produtos</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${sessionScope.userNivel <= 9}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/produtos/form">Novo produto</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty produtos}">
                <p class="empty-state">Nenhum produto cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Descrição</th>
                            <th>Periodicidade</th>
                            <th>Última execução</th>
                            <th>Fornecedor da última atualização</th>
                            <th>Valor pago</th>
                            <th>Fornecedores que oferecem</th>
                            <th>Próxima execução (calculada)</th>
                            <th>Dias faltantes</th>
                            <th>Próxima execução (agendada)</th>
                            <th>Fornecedor da próxima execução</th>
                            <th>Valor orçado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${produtos}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/produtos/form?id=${p.id}'">
                                <td>${p.descricao}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:otherwise>A cada ${p.periodicidade} ${p.unidadePeriodicidade.label}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty p.ultimaExecucao ? '-' : p.ultimaExecucao}</td>
                                <td>${empty p.ultimoFornecedorNome ? '-' : p.ultimoFornecedorNome}</td>
                                <td><c:choose><c:when test="${empty p.valorPagoUltimaExecucao}">-</c:when><c:otherwise>R$ ${p.valorPagoUltimaExecucao}</c:otherwise></c:choose></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty p.fornecedoresQueOferecem}">-</c:when>
                                        <c:otherwise>
                                            <c:forEach var="f" items="${p.fornecedoresQueOferecem}" varStatus="status">
                                                ${f.nomeRazaoSocial}<c:if test="${not status.last}"><br></c:if>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="${p.atrasado ? 'atrasado' : ''}">
                                    <c:choose>
                                        <c:when test="${p.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:when test="${empty p.proximaExecucao}">Nunca executado</c:when>
                                        <c:otherwise>${p.proximaExecucao}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="${p.atrasado ? 'atrasado' : ''}">
                                    <c:choose>
                                        <c:when test="${p.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:when test="${empty p.ultimaExecucao}">Nunca executado</c:when>
                                        <c:when test="${p.diasFaltantes lt 0}">Atrasado há ${-1 * p.diasFaltantes} dia(s)</c:when>
                                        <c:otherwise>${p.diasFaltantes} dia(s)</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty p.dataAgendadaProximaExecucao ? '-' : p.dataAgendadaProximaExecucao}</td>
                                <td>${empty p.fornecedorProximaExecucaoNome ? '-' : p.fornecedorProximaExecucaoNome}</td>
                                <td><c:choose><c:when test="${empty p.valorOrcadoProximaExecucao}">-</c:when><c:otherwise>R$ ${p.valorOrcadoProximaExecucao}</c:otherwise></c:choose></td>
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
