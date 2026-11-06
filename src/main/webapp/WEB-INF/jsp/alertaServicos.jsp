<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="temAgenda" value="${not empty agendaHoje or not empty agendaPendentesAnteriores}" />
<c:set var="totalColunas" value="${(empty servicosAtrasados ? 0 : 1) + (temAgenda ? 1 : 0)}" />
<c:set var="multiplasColunas" value="${totalColunas > 1}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Avisos de serviços - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=33">
</head>
<body>
<div class="page">
    <div class="card ${multiplasColunas ? 'card-wide' : ''}">
        <p class="condominio-nome">${nomeCondominio}</p>
        <h1 class="title">Atenção</h1>

        <div class="alerta-grid">
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

            <c:if test="${temAgenda}">
                <div class="alerta-coluna">
                    <p class="subtitle">Compromisso(s) da agenda para hoje</p>
                    <div class="row-list">
                        <c:forEach var="a" items="${agendaHoje}">
                            <div class="row-item" style="cursor:pointer;" title="Abrir este compromisso na agenda"
                                 onclick="window.location='${pageContext.request.contextPath}/agenda/form?id=${a.id}'">
                                <span class="agendado">
                                    <c:if test="${not empty a.horaFormatada}">${a.horaFormatada} - </c:if>${a.assunto}<c:if test="${not empty a.pontoFocal}"> (${a.pontoFocal})</c:if>
                                </span>
                            </div>
                        </c:forEach>
                        <c:if test="${empty agendaHoje}">
                            <div class="row-item"><span>Nenhum compromisso para hoje.</span></div>
                        </c:if>
                    </div>

                    <c:if test="${not empty agendaPendentesAnteriores}">
                        <label class="radio-option" style="margin-bottom:8px;">
                            <input type="checkbox" id="mostrarPendentesAnteriores"
                                   onchange="document.getElementById('pendentesAnteriores').hidden = !this.checked">
                            <span>Mostrar pendentes de datas anteriores (${agendaPendentesAnteriores.size()})</span>
                        </label>
                        <div id="pendentesAnteriores" class="row-list" hidden>
                            <c:forEach var="a" items="${agendaPendentesAnteriores}">
                                <div class="row-item" style="cursor:pointer;" title="Abrir este compromisso na agenda"
                                     onclick="window.location='${pageContext.request.contextPath}/agenda/form?id=${a.id}'">
                                    <span class="atrasado">
                                        ${a.dataFormatada}<c:if test="${not empty a.horaFormatada}"> ${a.horaFormatada}</c:if> - ${a.assunto}<c:if test="${not empty a.pontoFocal}"> (${a.pontoFocal})</c:if>
                                    </span>
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>
                </div>
            </c:if>
        </div>

        <a class="btn btn-primary" href="${pageContext.request.contextPath}/menu">Continuar para o menu</a>
    </div>
</div>
</body>
</html>
