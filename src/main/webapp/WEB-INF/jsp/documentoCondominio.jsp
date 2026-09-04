<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dados do condomínio - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">Dados do condomínio</h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">PDF com os dados cadastrais do condomínio.</p>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${temArquivo}">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/documento-condominio/arquivo" target="_blank">
                    Ver arquivo atual
                </a>
            </c:when>
            <c:otherwise>
                <p class="empty-state">Nenhum documento cadastrado ainda.</p>
            </c:otherwise>
        </c:choose>

        <c:if test="${!readOnly}">
            <form method="post" action="${pageContext.request.contextPath}/documento-condominio" enctype="multipart/form-data">
                <div class="field">
                    <label for="arquivo">${temArquivo ? 'Substituir arquivo (PDF)' : 'Selecionar arquivo (PDF)'}</label>
                    <input type="file" id="arquivo" name="arquivo" accept="application/pdf">
                </div>
                <button type="submit" class="btn btn-primary">Enviar</button>
            </form>
        </c:if>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
</body>
</html>
