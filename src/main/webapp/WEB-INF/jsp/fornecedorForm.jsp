<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Fornecedor - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <h1 class="title">${empty param.id ? 'Novo fornecedor' : 'Editar fornecedor'}</h1>
        <p class="subtitle">Dados do fornecedor</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/fornecedores/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="nomeRazaoSocial">Nome ou razao social</label>
                <input type="text" id="nomeRazaoSocial" name="nomeRazaoSocial" value="${fornecedor.nomeRazaoSocial}" required>
                <c:if test="${not empty errors.nomeRazaoSocial}"><div class="field-error">${errors.nomeRazaoSocial}</div></c:if>
            </div>

            <div class="field">
                <label for="telefones">Telefones</label>
                <textarea id="telefones" name="telefones" placeholder="Um ou mais numeros, um por linha">${fornecedor.telefones}</textarea>
            </div>

            <div class="field">
                <label for="contato1Nome">Contato 1 - nome</label>
                <input type="text" id="contato1Nome" name="contato1Nome" value="${fornecedor.contato1Nome}">
            </div>
            <div class="field">
                <label for="contato1Cargo">Contato 1 - cargo</label>
                <input type="text" id="contato1Cargo" name="contato1Cargo" value="${fornecedor.contato1Cargo}">
            </div>

            <div class="field">
                <label for="contato2Nome">Contato 2 - nome</label>
                <input type="text" id="contato2Nome" name="contato2Nome" value="${fornecedor.contato2Nome}">
            </div>
            <div class="field">
                <label for="contato2Cargo">Contato 2 - cargo</label>
                <input type="text" id="contato2Cargo" name="contato2Cargo" value="${fornecedor.contato2Cargo}">
            </div>

            <div class="field">
                <label for="contato3Nome">Contato 3 - nome</label>
                <input type="text" id="contato3Nome" name="contato3Nome" value="${fornecedor.contato3Nome}">
            </div>
            <div class="field">
                <label for="contato3Cargo">Contato 3 - cargo</label>
                <input type="text" id="contato3Cargo" name="contato3Cargo" value="${fornecedor.contato3Cargo}">
            </div>

            <div class="field">
                <label for="classificacao">Classificacao</label>
                <select id="classificacao" name="classificacao" required>
                    <option value="" disabled ${empty fornecedor.classificacao ? 'selected' : ''}>Selecione...</option>
                    <option value="muito_bom" ${fornecedor.classificacao == 'MUITO_BOM' ? 'selected' : ''}>Muito bom</option>
                    <option value="bom" ${fornecedor.classificacao == 'BOM' ? 'selected' : ''}>Bom</option>
                    <option value="medio" ${fornecedor.classificacao == 'MEDIO' ? 'selected' : ''}>Medio</option>
                    <option value="ruim" ${fornecedor.classificacao == 'RUIM' ? 'selected' : ''}>Ruim</option>
                    <option value="muito_ruim" ${fornecedor.classificacao == 'MUITO_RUIM' ? 'selected' : ''}>Muito ruim</option>
                </select>
                <c:if test="${not empty errors.classificacao}"><div class="field-error">${errors.classificacao}</div></c:if>
            </div>

            <div class="field">
                <label>Produtos fornecidos por este fornecedor</label>
                <c:choose>
                    <c:when test="${empty produtos}">
                        <p class="empty-state">Nenhum produto cadastrado ainda.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="row-list">
                            <c:forEach var="produto" items="${produtos}">
                                <label class="row-item">
                                    <input type="checkbox" name="produtoIds" value="${produto.id}"
                                           ${fornecedor.produtoIds.contains(produto.id) ? 'checked' : ''}>
                                    <span>${produto.descricao}</span>
                                </label>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="field">
                <label for="observacao">Observacao</label>
                <textarea id="observacao" name="observacao" style="min-height:160px;">${fornecedor.observacao}</textarea>
            </div>

            <button type="submit" class="btn btn-primary">Salvar</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/fornecedores">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>
    </div>
</div>
</body>
</html>
