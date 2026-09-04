<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Serviços de manutenção - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Serviços de manutenção</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/servicos/form">Novo serviço</a>
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
            <c:when test="${empty servicos}">
                <p class="empty-state">Nenhum serviço cadastrado ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Descrição</th>
                            <th>Period.</th>
                            <th>Tipo</th>
                            <th>Últ exec</th>
                            <th>Ult prest</th>
                            <th>Agend</th>
                            <th>Data limite</th>
                            <th style="min-width: 320px;">Observação</th>
                            <th>Dias</th>
                            <th>Prestador prox</th>
                            <th>Valor orçado</th>
                            <th>Histórico</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${servicos}">
                            <tr class="clickable-row ${s.atrasado ? 'row-atrasado' : ''}"
                                onclick="window.location='${pageContext.request.contextPath}/servicos/form?id=${s.id}'">
                                <td>${s.descricao}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Demanda</c:when>
                                        <c:otherwise>${s.periodicidade} ${s.unidadePeriodicidade.label}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${s.tipo.label}</td>
                                <td>${empty s.ultimaExecucao ? '-' : s.ultimaExecucaoFormatada}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty s.ultimoPrestadorNome}">-</c:when>
                                        <c:otherwise>
                                            ${s.ultimoPrestadorNome}
                                            <c:if test="${not empty s.ultimoPrestadorClassificacao}">
                                                <br><span class="badge badge-${s.ultimoPrestadorClassificacao.dbValue}">${s.ultimoPrestadorClassificacao.label}</span>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty s.dataAgendadaProximaExecucao}">-</c:when>
                                        <c:otherwise>
                                            ${s.dataAgendadaProximaExecucaoFormatada}
                                            <c:if test="${not empty s.prestadorProximaExecucaoNome}">
                                                <br>${s.prestadorProximaExecucaoNome}
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Demanda</c:when>
                                        <c:when test="${empty s.proximaExecucao}">Nunca executado</c:when>
                                        <c:otherwise>${s.proximaExecucaoFormatada}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.observacao ? '-' : s.observacao}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.unidadePeriodicidade == 'POR_DEMANDA'}">Demanda</c:when>
                                        <c:when test="${empty s.ultimaExecucao}">Nunca executado</c:when>
                                        <c:when test="${s.diasFaltantes lt 0}">Atrasado há ${-1 * s.diasFaltantes} dia(s)</c:when>
                                        <c:otherwise>${s.diasFaltantes} dia(s)</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${empty s.prestadorProximaExecucaoNome ? '-' : s.prestadorProximaExecucaoNome}</td>
                                <td><c:choose><c:when test="${empty s.valorOrcadoProximaExecucao}">-</c:when><c:otherwise>R$ ${s.valorOrcadoProximaExecucao}</c:otherwise></c:choose></td>
                                <td>
                                    <a class="icon-link" title="Histórico deste serviço" aria-label="Histórico deste serviço"
                                       href="${pageContext.request.contextPath}/pendencias-servico?servicoId=${s.id}"
                                       onclick="event.stopPropagation();">📋</a>
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
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
</body>
</html>
