<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nova senha - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card">
        <h1 class="title">Digite o código</h1>
        <p class="subtitle">
            Enviamos um código de 6 dígitos (simulado) para
            ${sessionScope.resetMethod == 'sms' ? 'o telefone' : 'o e-mail'} ${sessionScope.resetDestination}
        </p>

        <div class="simulated-code">
            Como este app ainda não tem um provedor real de SMS/e-mail conectado,<br>
            o código gerado é mostrado aqui mesmo: <strong>${sessionScope.simulatedCode}</strong><br>
            Válido por 10 minutos.
        </div>

        <c:if test="${not empty resentMessage}">
            <p class="form-info">${resentMessage}</p>
        </c:if>
        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/reset-password">
            <div class="field">
                <label for="code">Código de verificação</label>
                <input type="text" id="code" name="code" placeholder="000000" maxlength="6" inputmode="numeric" required>
                <c:if test="${not empty errors.code}"><div class="field-error">${errors.code}</div></c:if>
            </div>
            <div class="field">
                <label for="password">Nova senha</label>
                <input type="password" id="password" name="password" placeholder="Mínimo 6 caracteres" required>
                <c:if test="${not empty errors.password}"><div class="field-error">${errors.password}</div></c:if>
            </div>
            <div class="field">
                <label for="confirmPassword">Confirmar nova senha</label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Repita a nova senha" required>
                <c:if test="${not empty errors.confirmPassword}"><div class="field-error">${errors.confirmPassword}</div></c:if>
            </div>

            <button type="submit" class="btn btn-primary">Redefinir senha</button>
        </form>

        <form method="post" action="${pageContext.request.contextPath}/reset-password">
            <input type="hidden" name="resend" value="1">
            <button type="submit" class="btn btn-secondary">Reenviar código</button>
        </form>
    </div>
</div>
</body>
</html>
