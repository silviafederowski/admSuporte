<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "fornecedores") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fornecedor - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=30">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar fornecedor</c:when>
                    <c:when test="${empty param.id}">Novo fornecedor</c:when>
                    <c:otherwise>Editar fornecedor</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <button type="submit" form="fornecedorForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/fornecedores" title="Cancelar" aria-label="Cancelar">↩️</a>
                <c:if test="${!readOnly && not empty param.id}">
                    <button type="submit" form="fornecedorExcluirForm" class="menu-icon-link" title="Excluir" aria-label="Excluir">🗑️</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form id="fornecedorForm" method="post" action="${pageContext.request.contextPath}/fornecedores/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="nomeRazaoSocial">Nome ou razão social</label>
                <input type="text" id="nomeRazaoSocial" name="nomeRazaoSocial" value="${fornecedor.nomeRazaoSocial}" ${dis} required>
                <c:if test="${not empty errors.nomeRazaoSocial}"><div class="field-error">${errors.nomeRazaoSocial}</div></c:if>
            </div>

            <div class="field">
                <label for="email">Email</label>
                <textarea id="email" name="email" placeholder="Um ou mais e-mails, um por linha" ${dis}>${fornecedor.email}</textarea>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato1Nome">Contato 1 - nome</label>
                    <input type="text" id="contato1Nome" name="contato1Nome" value="${fornecedor.contato1Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato1Cargo">Cargo</label>
                    <input type="text" id="contato1Cargo" name="contato1Cargo" value="${fornecedor.contato1Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato1Telefone">Telefone</label>
                    <input type="text" id="contato1Telefone" name="contato1Telefone" value="${fornecedor.contato1Telefone}" ${dis}>
                </div>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato2Nome">Contato 2 - nome</label>
                    <input type="text" id="contato2Nome" name="contato2Nome" value="${fornecedor.contato2Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato2Cargo">Cargo</label>
                    <input type="text" id="contato2Cargo" name="contato2Cargo" value="${fornecedor.contato2Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato2Telefone">Telefone</label>
                    <input type="text" id="contato2Telefone" name="contato2Telefone" value="${fornecedor.contato2Telefone}" ${dis}>
                </div>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato3Nome">Contato 3 - nome</label>
                    <input type="text" id="contato3Nome" name="contato3Nome" value="${fornecedor.contato3Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato3Cargo">Cargo</label>
                    <input type="text" id="contato3Cargo" name="contato3Cargo" value="${fornecedor.contato3Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato3Telefone">Telefone</label>
                    <input type="text" id="contato3Telefone" name="contato3Telefone" value="${fornecedor.contato3Telefone}" ${dis}>
                </div>
            </div>

            <div class="field">
                <label for="classificacao">Classificação</label>
                <select id="classificacao" name="classificacao" ${dis} required>
                    <option value="nao_avaliado" ${empty fornecedor.classificacao || fornecedor.classificacao == 'NAO_AVALIADO' ? 'selected' : ''}>Não avaliado</option>
                    <option value="muito_bom" ${fornecedor.classificacao == 'MUITO_BOM' ? 'selected' : ''}>Muito bom</option>
                    <option value="bom" ${fornecedor.classificacao == 'BOM' ? 'selected' : ''}>Bom</option>
                    <option value="medio" ${fornecedor.classificacao == 'MEDIO' ? 'selected' : ''}>Médio</option>
                    <option value="ruim" ${fornecedor.classificacao == 'RUIM' ? 'selected' : ''}>Ruim</option>
                    <option value="muito_ruim" ${fornecedor.classificacao == 'MUITO_RUIM' ? 'selected' : ''}>Muito ruim</option>
                </select>
                <c:if test="${not empty errors.classificacao}"><div class="field-error">${errors.classificacao}</div></c:if>
            </div>

            <div class="field">
                <label>Tipos de produto fornecidos por este fornecedor</label>
                <c:choose>
                    <c:when test="${empty tipos}">
                        <p class="empty-state">Nenhum tipo de produto cadastrado ainda.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="row-list">
                            <c:forEach var="tipo" items="${tipos}">
                                <label class="row-item">
                                    <input type="checkbox" name="tipoProdutoIds" value="${tipo.id}" ${dis}
                                           ${fornecedor.tipoProdutoIds.contains(tipo.id) ? 'checked' : ''}>
                                    <span>${tipo.descricao}</span>
                                </label>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="field">
                <label for="observacao">Observação</label>
                <textarea id="observacao" name="observacao" style="min-height:160px;" ${dis}>${fornecedor.observacao}</textarea>
            </div>

        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form id="fornecedorExcluirForm" method="post" action="${pageContext.request.contextPath}/fornecedores/excluir"
                  onsubmit="return confirm('Excluir este fornecedor?');">
                <input type="hidden" name="id" value="${param.id}">
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
