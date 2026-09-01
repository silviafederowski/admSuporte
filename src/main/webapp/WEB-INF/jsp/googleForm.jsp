<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Formulário Google - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card menu-page">
        <img class="logo" src="${pageContext.request.contextPath}/logo" alt="Logotipo">
        <h1 class="title">Formulário Google</h1>
        <p class="subtitle">
            Selecione uma planilha (.xlsx). As linhas serão ordenadas pela
            coluna D e exibidas para você escolher quais entram no PDF.
        </p>

        <c:if test="${not empty error}">
            <p class="form-error">${error}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/google-form/upload" enctype="multipart/form-data">
            <div class="field">
                <label for="file">Arquivo da planilha</label>
                <input type="file" id="file" name="file" accept=".xlsx,.xls" required>
            </div>
            <button type="submit" class="btn btn-primary">Processar arquivo</button>
        </form>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
</body>
</html>
