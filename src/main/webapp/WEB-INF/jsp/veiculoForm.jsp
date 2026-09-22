<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Veículos - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=30">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                Veículos da unidade ${unidade.codigo}
                <c:if test="${not empty codigosVagaUnidade}"> - Vaga ${codigosVagaUnidade}</c:if>
            </h1>
            <div class="toolbar-actions">
                <button type="submit" form="veiculoForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/veiculos" title="Cancelar" aria-label="Cancelar">↩️</a>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>
        <div class="field-inline">
            <label for="obs">Obs:</label>
            <input type="text" id="obs" name="obs" form="veiculoForm" maxlength="45" value="${obs}">
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="veiculoForm" method="post" action="${pageContext.request.contextPath}/veiculos/form">
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
                    <div class="field veiculo-row-delete">
                        <button type="button" class="icon-link" title="Remover este veículo" aria-label="Remover este veículo"
                                onclick="limparLinhaVeiculo(${status.count})">🗑️</button>
                    </div>
                </div>
                <c:if test="${not empty errors[linhaKey]}">
                    <div class="field-error">${errors[linhaKey]}</div>
                </c:if>
            </c:forEach>

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

        window.limparLinhaVeiculo = function (row) {
            if (!confirm('Remover este veículo? A remoção só é efetivada ao clicar em Salvar.')) {
                return;
            }
            document.getElementById('marcaId' + row).value = '';
            document.getElementById('modeloId' + row).value = '';
            document.getElementById('corId' + row).value = '';
            document.getElementById('placas' + row).value = '';
            filterModelos(row);
        };
    })();
</script>
</body>
</html>
