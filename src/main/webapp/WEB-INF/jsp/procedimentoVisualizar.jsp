<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${not empty arquivoNome ? arquivoNome : 'Documento'} - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">${not empty arquivoNome ? arquivoNome : 'Documento'}</h1>
            </div>
            <div class="toolbar-actions">
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <div class="drive-preview-wrap">
            <c:choose>
                <c:when test="${arquivoMimeType == 'application/vnd.google-apps.document'}">
                    <iframe class="drive-preview" src="https://docs.google.com/document/d/${arquivoId}/preview"></iframe>
                </c:when>
                <c:when test="${arquivoMimeType == 'application/vnd.google-apps.spreadsheet'}">
                    <iframe class="drive-preview" src="https://docs.google.com/spreadsheets/d/${arquivoId}/preview"></iframe>
                </c:when>
                <c:when test="${arquivoMimeType == 'application/vnd.google-apps.presentation'}">
                    <iframe class="drive-preview" src="https://docs.google.com/presentation/d/${arquivoId}/preview"></iframe>
                </c:when>
                <c:otherwise>
                    <iframe class="drive-preview" src="https://drive.google.com/file/d/${arquivoId}/preview"></iframe>
                </c:otherwise>
            </c:choose>
        </div>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/procedimentos">Voltar aos documentos</a>
        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
</body>
</html>
