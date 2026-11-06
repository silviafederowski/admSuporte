<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Checklist - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=34">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Checklist</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/checklist-portaria/form" title="Novo checklist" aria-label="Novo checklist">➕</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/checklist-portaria/pdf" title="PDF do checklist em branco, para preencher à mão" aria-label="PDF do checklist em branco">🖨️</a>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>
        <p class="subtitle">Clique em uma linha para ver os itens daquele checklist.</p>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty checklists}">
                <p class="empty-state">Nenhum checklist feito ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <%-- Ja vem do servidor em ordem decrescente de inclusao (sem data-sort-desc, que reordenaria por Data). --%>
                        <tr>
                            <th data-no-sort title="Validar">🔎</th>
                            <th>Data</th>
                            <th>Hora</th>
                            <th>Usuário</th>
                            <th>Itens checados</th>
                            <th title="Encomendas sem baixa no aplicativo">Encom. s/ baixa</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="r" items="${checklists}">
                            <c:url var="urlChecklist" value="/checklist-portaria/form">
                                <c:param name="data" value="${r.data}" />
                                <c:param name="usuario" value="${r.usuario}" />
                            </c:url>
                            <tr class="clickable-row" onclick="window.location='${urlChecklist}'">
                                <td onclick="event.stopPropagation();">
                                    <%-- Validar: so "disponivel para validacao" e nunca o proprio autor (o servidor confere de novo). --%>
                                    <c:if test="${!readOnly && r.status == 'disponivel' && r.usuario != sessionScope.userName}">
                                        <c:url var="urlValidar" value="/checklist-portaria/form">
                                            <c:param name="data" value="${r.data}" />
                                            <c:param name="usuario" value="${r.usuario}" />
                                            <c:param name="validar" value="1" />
                                        </c:url>
                                        <a class="menu-icon-link" href="${urlValidar}" title="Validar este checklist" aria-label="Validar este checklist">🔎</a>
                                    </c:if>
                                </td>
                                <td>${r.dataFormatada}</td>
                                <td>${r.horaFormatada}</td>
                                <td>${r.usuario}</td>
                                <td>
                                    <span class="badge ${r.completo ? 'badge-resolvido' : 'badge-pendente'}">${r.itensChecados} de ${r.totalItens}</span>
                                </td>
                                <td>${empty r.encomendasSemBaixa ? '-' : r.encomendasSemBaixa}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${r.verificado}"><span class="badge badge-resolvido" title="Validada por ${r.verificadoPor}">Validada</span></c:when>
                                        <c:when test="${r.status == 'disponivel'}"><span class="badge badge-prioridade-baixa">Disponível p/ validação</span></c:when>
                                        <c:otherwise><span class="badge badge-pendente">Em andamento</span></c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/dataTable.js?v=7"></script>
</body>
</html>
