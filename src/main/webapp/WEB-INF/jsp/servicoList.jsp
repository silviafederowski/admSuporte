<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Serviços de manutenção - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Serviços de manutenção</h1>
            </div>
            <c:if test="${sessionScope.userNivel <= 9}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos/form">Novo serviço</a>
            </c:if>
        </div>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty servicos}">
                <p class="empty-state">Nenhum serviço cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Descrição</th>
                            <th>Periodicidade</th>
                            <th>Última execução</th>
                            <th>Prestador da última atualização</th>
                            <th>Valor pago</th>
                            <th>Prestadores que oferecem</th>
                            <th>Próxima execução (calculada)</th>
                            <th>Dias faltantes</th>
                            <th>Próxima execução (agendada)</th>
                            <th>Prestador da próxima execução</th>
                            <th>Valor orçado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${servicos}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/servicos/form?id=${s.id}'">
                                <td>${s.descricao}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:otherwise>A cada ${s.periodicidade} ${s.unidadePeriodicidade.label}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.ultimaExecucao ? '-' : s.ultimaExecucao}</td>
                                <td>${empty s.ultimoPrestadorNome ? '-' : s.ultimoPrestadorNome}</td>
                                <td><c:choose><c:when test="${empty s.valorPagoUltimaExecucao}">-</c:when><c:otherwise>R$ ${s.valorPagoUltimaExecucao}</c:otherwise></c:choose></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty s.prestadoresQueOferecem}">-</c:when>
                                        <c:otherwise>
                                            <c:forEach var="p" items="${s.prestadoresQueOferecem}" varStatus="status">
                                                ${p.nomeRazaoSocial}<c:if test="${not status.last}"><br></c:if>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="${s.atrasado ? 'atrasado' : ''}">
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:when test="${empty s.proximaExecucao}">Nunca executado</c:when>
                                        <c:otherwise>${s.proximaExecucao}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="${s.atrasado ? 'atrasado' : ''}">
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Por demanda</c:when>
                                        <c:when test="${empty s.ultimaExecucao}">Nunca executado</c:when>
                                        <c:when test="${s.diasFaltantes lt 0}">Atrasado há ${-1 * s.diasFaltantes} dia(s)</c:when>
                                        <c:otherwise>${s.diasFaltantes} dia(s)</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.dataAgendadaProximaExecucao ? '-' : s.dataAgendadaProximaExecucao}</td>
                                <td>${empty s.prestadorProximaExecucaoNome ? '-' : s.prestadorProximaExecucaoNome}</td>
                                <td><c:choose><c:when test="${empty s.valorOrcadoProximaExecucao}">-</c:when><c:otherwise>R$ ${s.valorOrcadoProximaExecucao}</c:otherwise></c:choose></td>
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
