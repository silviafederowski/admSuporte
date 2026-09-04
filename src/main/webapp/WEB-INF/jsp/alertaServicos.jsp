<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ambosPresentes" value="${not empty servicosAtrasados && not empty servicosAgendadosHoje}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Avisos de serviços - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card ${ambosPresentes ? 'card-wide' : ''}">
        <h1 class="title">Atenção</h1>

        <div class="alerta-grid">
            <c:if test="${not empty servicosAgendadosHoje}">
                <div class="alerta-coluna">
                    <p class="subtitle">Serviço(s) agendado(s) para hoje</p>
                    <div class="row-list">
                        <c:forEach var="s" items="${servicosAgendadosHoje}">
                            <div class="row-item">
                                <span class="agendado">
                                    ${s.descricao}<c:if test="${not empty s.prestadorProximaExecucaoNome}"> - ${s.prestadorProximaExecucaoNome}</c:if>
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty servicosAtrasados}">
                <div class="alerta-coluna">
                    <p class="subtitle">Existe(m) serviço(s) atrasado(s)</p>
                    <div class="row-list">
                        <c:forEach var="s" items="${servicosAtrasados}">
                            <div class="row-item">
                                <span class="atrasado">
                                    ${s.descricao} -
                                    <c:choose>
                                        <c:when test="${empty s.ultimaExecucao}">nunca executado</c:when>
                                        <c:otherwise>atrasado há ${-1 * s.diasFaltantes} dia(s)</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </div>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/menu">Continuar para o menu</a>
    </div>
</div>
</body>
</html>
