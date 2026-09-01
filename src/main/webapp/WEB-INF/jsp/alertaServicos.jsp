<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Serviços atrasados - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <h1 class="title">Atenção</h1>
        <p class="subtitle">Existe(m) serviço(s) de manutenção atrasado(s)</p>

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

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/menu">Continuar para o menu</a>
    </div>
</div>
</body>
</html>
