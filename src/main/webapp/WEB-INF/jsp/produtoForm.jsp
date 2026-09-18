<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "produtos") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Produto - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=19">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar produto</c:when>
                    <c:when test="${empty param.id}">Novo produto</c:when>
                    <c:otherwise>Editar produto</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="produtoForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/produtos" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="produtoExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="produtoForm" method="post" action="${pageContext.request.contextPath}/produtos/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field-row produto-descricao-row">
                <div class="field">
                    <label for="descricao">Descrição</label>
                    <input type="text" id="descricao" name="descricao" value="${produto.descricao}" ${dis} required>
                    <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
                </div>
                <div class="field">
                    <label for="tipoId">Tipo</label>
                    <select id="tipoId" name="tipoId" ${dis}>
                        <option value="" ${empty produto.tipoId ? 'selected' : ''}>Não informado</option>
                        <c:forEach var="t" items="${tipos}">
                            <option value="${t.id}" ${produto.tipoId == t.id ? 'selected' : ''}>${t.descricao}</option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.tipoId}"><div class="field-error">${errors.tipoId}</div></c:if>
                </div>
            </div>

            <div class="field-row produto-estoque-row">
                <div class="field">
                    <label for="unidade">Unidade</label>
                    <select id="unidade" name="unidade" ${dis} required>
                        <option value="" disabled ${empty produto.unidade ? 'selected' : ''}>Selecione...</option>
                        <option value="unidades" ${produto.unidade == 'UNIDADES' ? 'selected' : ''}>Unidades</option>
                        <option value="duzias" ${produto.unidade == 'DUZIAS' ? 'selected' : ''}>Dúzias</option>
                        <option value="kgs" ${produto.unidade == 'KGS' ? 'selected' : ''}>Kgs</option>
                        <option value="litros" ${produto.unidade == 'LITROS' ? 'selected' : ''}>Litros</option>
                    </select>
                    <c:if test="${not empty errors.unidade}"><div class="field-error">${errors.unidade}</div></c:if>
                </div>
                <div class="field">
                    <label for="conversaoUnidades">Conversão p/ unidades</label>
                    <input type="number" id="conversaoUnidades" name="conversaoUnidades" class="campo-estoque" step="0.01" min="0.01" ${dis}
                           value="${produto.conversaoUnidades}" oninput="recalcularComprar()">
                    <c:if test="${not empty errors.conversaoUnidades}"><div class="field-error">${errors.conversaoUnidades}</div></c:if>
                </div>
                <div class="field">
                    <label for="estoqueIdeal">Estoque ideal</label>
                    <input type="number" id="estoqueIdeal" name="estoqueIdeal" class="campo-estoque" step="0.01" min="0" ${dis}
                           value="${produto.estoqueIdeal}" oninput="recalcularComprar()">
                    <c:if test="${not empty errors.estoqueIdeal}"><div class="field-error">${errors.estoqueIdeal}</div></c:if>
                </div>
            </div>

            <div class="field-inline">
                <label for="estoqueMinimo">Mínimo(unid)</label>
                <input type="number" id="estoqueMinimo" name="estoqueMinimo" class="campo-estoque" step="1" min="0" ${dis}
                       style="flex: none; width: 90px;" value="${produto.estoqueMinimo}" oninput="recalcularComprar()">
                <label for="estoqueAtual">Atual(unid)</label>
                <input type="number" id="estoqueAtual" name="estoqueAtual" class="campo-estoque" step="1" min="0" ${dis}
                       style="flex: none; width: 90px;" value="${produto.estoqueAtual}" oninput="recalcularComprar()">
                <label for="comprar">Comprar</label>
                <input type="number" id="comprar" name="comprar" class="campo-estoque" step="0.01" min="0" ${dis}
                       style="flex: none; width: 90px;" value="${produto.comprar}">
                <span>${produto.unidade.label}</span>
            </div>
            <c:if test="${not empty errors.estoqueMinimo}"><div class="field-error">${errors.estoqueMinimo}</div></c:if>
            <c:if test="${not empty errors.estoqueAtual}"><div class="field-error">${errors.estoqueAtual}</div></c:if>
            <c:if test="${not empty errors.comprar}"><div class="field-error">${errors.comprar}</div></c:if>

            <script>
                function recalcularComprar() {
                    var ideal = parseFloat(document.getElementById('estoqueIdeal').value);
                    var minimo = parseInt(document.getElementById('estoqueMinimo').value, 10);
                    var atual = parseInt(document.getElementById('estoqueAtual').value, 10);
                    var conversao = parseFloat(document.getElementById('conversaoUnidades').value);
                    if (isNaN(ideal) || isNaN(minimo) || isNaN(atual) || isNaN(conversao) || conversao <= 0) {
                        return;
                    }
                    var sugestao = 0;
                    if (atual < minimo) {
                        sugestao = Math.max(Math.ceil(((ideal * conversao) - atual) / conversao), 0);
                    }
                    document.getElementById('comprar').value = sugestao;
                }
            </script>

            <div class="field">
                <label for="ultimoFornecedorId">Fornecedor da última compra</label>
                <select id="ultimoFornecedorId" name="ultimoFornecedorId" ${dis}>
                    <option value="" ${empty produto.ultimoFornecedorId ? 'selected' : ''}>Não informado</option>
                    <c:forEach var="f" items="${fornecedores}">
                        <option value="${f.id}" ${produto.ultimoFornecedorId == f.id ? 'selected' : ''}>${f.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.ultimoFornecedorId}"><div class="field-error">${errors.ultimoFornecedorId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorUltimaCompra">Valor última compra</label>
                <input type="number" id="valorUltimaCompra" name="valorUltimaCompra" step="0.01" min="0" ${dis}
                       value="${produto.valorUltimaCompra}">
                <c:if test="${not empty errors.valorUltimaCompra}"><div class="field-error">${errors.valorUltimaCompra}</div></c:if>
            </div>

        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="produtoExcluirForm" method="post" action="${pageContext.request.contextPath}/produtos/excluir"
                  onsubmit="return confirm('Excluir este produto?');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
