<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "unidades") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Unidade - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=30">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar unidade</c:when>
                    <c:when test="${empty param.id}">Nova unidade</c:when>
                    <c:otherwise>Editar unidade</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="unidadeForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/unidades" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="unidadeExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="unidadeForm" method="post" action="${pageContext.request.contextPath}/unidades/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row" style="grid-template-columns: 1fr 1fr;">
                <div class="field">
                    <label for="chave">Chave</label>
                    <c:choose>
                        <c:when test="${empty param.id}">
                            <input type="number" id="chave" name="chave" value="${unidade.chave}" ${dis} required>
                        </c:when>
                        <c:otherwise>
                            <input type="number" id="chave" value="${unidade.chave}" disabled>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${not empty errors.chave}"><div class="field-error">${errors.chave}</div></c:if>
                </div>
                <div class="field">
                    <label for="codigo">Código</label>
                    <input type="text" id="codigo" name="codigo" value="${unidade.codigo}" ${dis} required>
                    <c:if test="${not empty errors.codigo}"><div class="field-error">${errors.codigo}</div></c:if>
                </div>
            </div>

            <div class="field">
                <label for="vagasGaragem">Vagas de garagem</label>
                <select id="vagasGaragem" name="vagasGaragem" ${dis}>
                    <option value="1" ${unidade.vagasGaragem == 1 ? 'selected' : ''}>1</option>
                    <option value="2" ${unidade.vagasGaragem == 2 ? 'selected' : ''}>2</option>
                </select>
            </div>

            <div class="field">
                <label>
                    <input type="checkbox" name="candidataIdoso" ${unidade.candidataIdoso ? 'checked' : ''} ${dis}>
                    Candidata a vaga de idoso
                </label>
            </div>

        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="unidadeExcluirForm" method="post" action="${pageContext.request.contextPath}/unidades/excluir"
                  onsubmit="return confirm('Excluir esta unidade? Isso só é possível se não houver veículos cadastrados para ela.');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
