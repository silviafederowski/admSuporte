<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="readOnly" value="${sessionScope.userNivel != 0}" />
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Autorização - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar autorização</c:when>
                    <c:when test="${empty param.id}">Nova autorização</c:when>
                    <c:otherwise>Editar autorização</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">
            Código da tela usado nas URLs/filtros (ex.: servicos, prestadores, veiculos, vagas,
            documentos, usuarios, operacoes, google-form).
        </p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/autorizacoes/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row">
                <div class="field">
                    <label for="telaCodigo">Código da tela</label>
                    <input type="text" id="telaCodigo" name="telaCodigo" value="${autorizacao.telaCodigo}"
                           list="telaCodigosConhecidos" ${dis} required>
                    <datalist id="telaCodigosConhecidos">
                        <c:forEach var="entry" items="${descricoesPorTela}">
                            <option value="${entry.key}">
                        </c:forEach>
                    </datalist>
                    <c:if test="${not empty errors.telaCodigo}"><div class="field-error">${errors.telaCodigo}</div></c:if>
                </div>
                <div class="field">
                    <label for="nivel">Nível</label>
                    <select id="nivel" name="nivel" ${dis} required>
                        <option value="" disabled ${empty autorizacao.nivel ? 'selected' : ''}>Selecione...</option>
                        <c:forEach var="ut" items="${userTypes}">
                            <option value="${ut.nivel}" ${autorizacao.nivel == ut.nivel ? 'selected' : ''}>${ut.name} (${ut.nivel})</option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.nivel}"><div class="field-error">${errors.nivel}</div></c:if>
                </div>
                <div class="field">
                    <label for="tipoAcesso">Acesso</label>
                    <select id="tipoAcesso" name="tipoAcesso" ${dis} required>
                        <option value="" disabled ${empty autorizacao.tipoAcesso ? 'selected' : ''}>Selecione...</option>
                        <option value="consulta" ${autorizacao.tipoAcesso == 'consulta' ? 'selected' : ''}>Consulta</option>
                        <option value="edicao" ${autorizacao.tipoAcesso == 'edicao' ? 'selected' : ''}>Edição</option>
                    </select>
                    <c:if test="${not empty errors.tipoAcesso}"><div class="field-error">${errors.tipoAcesso}</div></c:if>
                </div>
            </div>

            <div class="field">
                <label for="telaDescricao">Breve descrição</label>
                <input type="text" id="telaDescricao" name="telaDescricao" value="${autorizacao.telaDescricao}" ${dis} required>
                <c:if test="${not empty errors.telaDescricao}"><div class="field-error">${errors.telaDescricao}</div></c:if>
            </div>

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/autorizacoes">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form method="post" action="${pageContext.request.contextPath}/autorizacoes/excluir"
                  onsubmit="return confirm('Excluir esta autorização?');">
                <input type="hidden" name="id" value="${param.id}">
                <button type="submit" class="btn btn-danger">Excluir</button>
            </form>
        </c:if>
    </div>
</div>
<script>
    var DESCRICOES_POR_TELA = {};
    <c:forEach var="entry" items="${descricoesPorTela}">
    DESCRICOES_POR_TELA["${entry.key}"] = "${entry.value}";
    </c:forEach>

    (function () {
        var telaCodigoInput = document.getElementById('telaCodigo');
        var telaDescricaoInput = document.getElementById('telaDescricao');
        if (!telaCodigoInput || !telaDescricaoInput) {
            return;
        }

        function autoPreencherDescricao() {
            var codigo = telaCodigoInput.value.trim().toLowerCase();
            var descricaoConhecida = DESCRICOES_POR_TELA[codigo];
            if (descricaoConhecida && telaDescricaoInput.value.trim() === '') {
                telaDescricaoInput.value = descricaoConhecida;
            }
        }

        telaCodigoInput.addEventListener('input', autoPreencherDescricao);
        telaCodigoInput.addEventListener('change', autoPreencherDescricao);
    })();
</script>
</body>
</html>
