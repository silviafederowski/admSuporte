<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Veículos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                Veículos da unidade ${unidade.codigo}
                <c:if test="${not empty codigosVagaUnidade}"> - ${codigosVagaUnidade}</c:if>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">Informe até 5 veículos. Cor, marca, modelo e placas são obrigatórios; deixe a linha em branco para não usá-la.</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/veiculos/form">
            <input type="hidden" name="unidadeChave" value="${unidade.chave}">

            <c:forEach var="v" items="${veiculos}" varStatus="status">
                <c:set var="linhaKey" value="linha${status.count}" />
                <div class="field-row veiculo-row">
                    <div class="field">
                        <label for="marcaId${status.count}">Marca</label>
                        <select id="marcaId${status.count}" name="marcaId${status.count}" class="marca-select" data-row="${status.count}">
                            <option value="" ${empty v.marcaId || v.marcaId == 0 ? 'selected' : ''}>Selecione...</option>
                            <c:forEach var="m" items="${marcas}">
                                <option value="${m.id}" ${v.marcaId == m.id ? 'selected' : ''}>${m.descricao}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="modeloId${status.count}">Modelo</label>
                        <select id="modeloId${status.count}" name="modeloId${status.count}" class="modelo-select" data-row="${status.count}">
                            <option value="" ${empty v.modeloId || v.modeloId == 0 ? 'selected' : ''}>Selecione...</option>
                            <c:forEach var="mo" items="${modelos}">
                                <option value="${mo.id}" data-marca="${mo.marcaId}" ${v.modeloId == mo.id ? 'selected' : ''}>${mo.descricao}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="corId${status.count}">Cor</label>
                        <select id="corId${status.count}" name="corId${status.count}">
                            <option value="" ${empty v.corId || v.corId == 0 ? 'selected' : ''}>Selecione...</option>
                            <c:forEach var="c" items="${cores}">
                                <option value="${c.id}" ${v.corId == c.id ? 'selected' : ''}>${c.descricao}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="placas${status.count}">Placas</label>
                        <input type="text" id="placas${status.count}" name="placas${status.count}" value="${v.placas}">
                    </div>
                </div>
                <c:if test="${not empty errors[linhaKey]}">
                    <div class="field-error">${errors[linhaKey]}</div>
                </c:if>
            </c:forEach>

            <button type="submit" class="btn btn-primary">Salvar</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/veiculos">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>
    </div>
</div>
<script>
    (function () {
        function filterModelos(row) {
            var marcaSelect = document.getElementById('marcaId' + row);
            var modeloSelect = document.getElementById('modeloId' + row);
            var marcaId = marcaSelect.value;
            var currentModelo = modeloSelect.value;
            var currentStillValid = false;

            Array.prototype.forEach.call(modeloSelect.options, function (option) {
                if (!option.value) {
                    return;
                }
                var matches = !marcaId || option.getAttribute('data-marca') === marcaId;
                option.hidden = !matches;
                if (matches && option.value === currentModelo) {
                    currentStillValid = true;
                }
            });

            if (!currentStillValid) {
                modeloSelect.value = '';
            }
        }

        var marcaSelects = document.querySelectorAll('.marca-select');
        marcaSelects.forEach(function (select) {
            var row = select.getAttribute('data-row');
            filterModelos(row);
            select.addEventListener('change', function () {
                filterModelos(row);
            });
        });
    })();
</script>
</body>
</html>
