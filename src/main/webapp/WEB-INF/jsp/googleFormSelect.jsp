<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Formulario Google - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <h1 class="title">Formulario Google</h1>
        <p class="subtitle">Selecione uma ou mais unidades para gerar o PDF</p>

        <c:if test="${not empty error}">
            <p class="form-error">${error}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/google-form/pdf">
            <button type="button" id="toggleAllBtn" class="btn btn-secondary" onclick="toggleAllRows()">Todos</button>

            <div class="row-list">
                <c:forEach var="row" items="${rows}" varStatus="status">
                    <label class="row-item">
                        <input type="checkbox" name="rowIndex" value="${status.index}" onchange="updateToggleAllBtn()">
                        <span>${row.label}</span>
                    </label>
                </c:forEach>
            </div>

            <button type="submit" class="btn btn-primary">Gerar PDF</button>
        </form>

        <script>
            function getRowCheckboxes() {
                return document.querySelectorAll('input[name="rowIndex"]');
            }

            function toggleAllRows() {
                var checkboxes = getRowCheckboxes();
                var allChecked = Array.prototype.every.call(checkboxes, function (cb) { return cb.checked; });
                checkboxes.forEach(function (cb) { cb.checked = !allChecked; });
                updateToggleAllBtn();
            }

            function updateToggleAllBtn() {
                var checkboxes = getRowCheckboxes();
                var allChecked = checkboxes.length > 0 && Array.prototype.every.call(checkboxes, function (cb) { return cb.checked; });
                document.getElementById('toggleAllBtn').textContent = allChecked ? 'Nenhum' : 'Todos';
            }
        </script>

        <form method="post" action="${pageContext.request.contextPath}/google-form">
            <input type="hidden" name="reset" value="1">
            <button type="submit" class="btn btn-secondary">Selecionar outro arquivo</button>
        </form>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>
</body>
</html>
