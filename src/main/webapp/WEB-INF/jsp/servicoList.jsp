<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Servicos de manutencao - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Servicos de manutencao</h1>
            </div>
            <c:if test="${sessionScope.userNivel <= 9}">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos/form">Novo servico</a>
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
                <p class="empty-state">Nenhum servico cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Descricao</th>
                            <th>Periodicidade</th>
                            <th>Ultima execucao</th>
                            <th>Prestador da ultima atualizacao</th>
                            <th>Valor pago</th>
                            <th>Prestadores que oferecem</th>
                            <th>Proxima execucao (calculada)</th>
                            <th>Dias faltantes</th>
                            <th>Proxima execucao (agendada)</th>
                            <th>Prestador da proxima execucao</th>
                            <th>Valor orcado</th>
                            <th data-no-sort>Acoes</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${servicos}">
                            <tr>
                                <td>${s.descricao}</td>
                                <td>A cada ${s.periodicidade} ${s.unidadePeriodicidade.label}</td>
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
                                    ${empty s.proximaExecucao ? 'Nunca executado' : s.proximaExecucao}
                                </td>
                                <td class="${s.atrasado ? 'atrasado' : ''}">
                                    <c:choose>
                                        <c:when test="${empty s.ultimaExecucao}">Nunca executado</c:when>
                                        <c:when test="${s.diasFaltantes lt 0}">Atrasado ha ${-1 * s.diasFaltantes} dia(s)</c:when>
                                        <c:otherwise>${s.diasFaltantes} dia(s)</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.dataAgendadaProximaExecucao ? '-' : s.dataAgendadaProximaExecucao}</td>
                                <td>${empty s.prestadorProximaExecucaoNome ? '-' : s.prestadorProximaExecucaoNome}</td>
                                <td><c:choose><c:when test="${empty s.valorOrcadoProximaExecucao}">-</c:when><c:otherwise>R$ ${s.valorOrcadoProximaExecucao}</c:otherwise></c:choose></td>
                                <td>
                                    <c:if test="${sessionScope.userNivel <= 9}">
                                        <div class="table-actions">
                                            <a class="btn btn-secondary btn-small" href="${pageContext.request.contextPath}/servicos/form?id=${s.id}">Editar</a>
                                            <form method="post" action="${pageContext.request.contextPath}/servicos/excluir"
                                                  onsubmit="return confirm('Excluir este servico?');">
                                                <input type="hidden" name="id" value="${s.id}">
                                                <button type="submit" class="btn btn-danger btn-small">Excluir</button>
                                            </form>
                                        </div>
                                    </c:if>
                                </td>
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
