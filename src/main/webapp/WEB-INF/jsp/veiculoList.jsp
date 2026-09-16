<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Veículos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=10">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Veículos</h1>
            </div>
            <div class="toolbar-actions">
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>
        <p class="subtitle">Clique em uma linha para cadastrar ou editar os veículos daquela unidade.</p>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty veiculos}">
                <p class="empty-state">Nenhuma unidade cadastrada ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Unidade</th>
                            <th>Vagas</th>
                            <th>Marca</th>
                            <th>Modelo</th>
                            <th>Cor</th>
                            <th>Placas</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:set var="unidadeAnterior" value="" />
                        <c:forEach var="v" items="${veiculos}">
                            <c:set var="primeiraDaUnidade" value="${v.unidadeCodigo != unidadeAnterior}" />
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/veiculos/form?unidade=${v.unidadeChave}'">
                                <td>
                                    <c:choose>
                                        <c:when test="${primeiraDaUnidade}">${v.unidadeCodigo}</c:when>
                                        <c:otherwise><span class="repeat-hidden">${v.unidadeCodigo}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${primeiraDaUnidade}">${v.vagaAtual}</c:when>
                                        <c:otherwise><span class="repeat-hidden">${v.vagaAtual}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${v.marcaDescricao}</td>
                                <td>${v.modeloDescricao}</td>
                                <td>${v.corDescricao}</td>
                                <td>${v.placas}</td>
                            </tr>
                            <c:set var="unidadeAnterior" value="${v.unidadeCodigo}" />
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
</body>
</html>
