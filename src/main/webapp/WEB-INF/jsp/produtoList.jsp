<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Produtos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=11">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Cadastro de produtos</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="${pageContext.request.contextPath}/produtos/form" title="Novo produto" aria-label="Novo produto">➕</a>
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
                            <th>Tipo</th>
                            <th>Unidade</th>
                            <th>Estoque mínimo</th>
                            <th>Estoque atual</th>
                            <th>Comprar</th>
                            <th>Fornecedor da última atualização</th>
                            <th>Valor última compra</th>
                            <th>Fornecedores que oferecem</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${produtos}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/produtos/form?id=${p.id}'">
                                <td>${p.descricao}</td>
                                <td>${empty p.tipoDescricao ? '-' : p.tipoDescricao}</td>
                                <td>${empty p.unidade ? '-' : p.unidade.label}</td>
                                <td>${p.estoqueMinimo}</td>
                                <td>${p.estoqueAtual}</td>
                                <td><span class="badge ${p.comprar ? 'badge-inativo' : 'badge-ativo'}">${p.comprar ? 'Sim' : 'Não'}</span></td>
                                <td>${empty p.ultimoFornecedorNome ? '-' : p.ultimoFornecedorNome}</td>
                                <td><c:choose><c:when test="${empty p.valorUltimaCompra}">-</c:when><c:otherwise>R$ ${p.valorUltimaCompra}</c:otherwise></c:choose></td>
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
                            </tr>
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
