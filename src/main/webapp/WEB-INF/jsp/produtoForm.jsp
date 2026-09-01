<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Produto - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <h1 class="title">${empty param.id ? 'Novo produto' : 'Editar produto'}</h1>
        <p class="subtitle">Dados do produto</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/produtos/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="descricao">Descricao</label>
                <input type="text" id="descricao" name="descricao" value="${produto.descricao}" required>
                <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
            </div>

            <div class="field">
                <label for="periodicidade">Periodicidade</label>
                <input type="number" id="periodicidade" name="periodicidade" min="1"
                       value="${not empty param.periodicidade ? param.periodicidade : produto.periodicidade}" required>
                <c:if test="${not empty errors.periodicidade}"><div class="field-error">${errors.periodicidade}</div></c:if>
            </div>

            <div class="field">
                <label for="unidadePeriodicidade">Unidade da periodicidade</label>
                <select id="unidadePeriodicidade" name="unidadePeriodicidade" required>
                    <option value="" disabled ${empty produto.unidadePeriodicidade ? 'selected' : ''}>Selecione...</option>
                    <option value="dia" ${produto.unidadePeriodicidade == 'DIA' ? 'selected' : ''}>Dias</option>
                    <option value="mes" ${produto.unidadePeriodicidade == 'MES' ? 'selected' : ''}>Meses</option>
                    <option value="ano" ${produto.unidadePeriodicidade == 'ANO' ? 'selected' : ''}>Anos</option>
                </select>
                <c:if test="${not empty errors.unidadePeriodicidade}"><div class="field-error">${errors.unidadePeriodicidade}</div></c:if>
            </div>

            <div class="field">
                <label for="ultimaExecucao">Data da ultima execucao</label>
                <input type="date" id="ultimaExecucao" name="ultimaExecucao" value="${produto.ultimaExecucao}">
                <c:if test="${not empty errors.ultimaExecucao}"><div class="field-error">${errors.ultimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="ultimoFornecedorId">Fornecedor da ultima atualizacao</label>
                <select id="ultimoFornecedorId" name="ultimoFornecedorId">
                    <option value="" ${empty produto.ultimoFornecedorId ? 'selected' : ''}>Nao informado</option>
                    <c:forEach var="f" items="${fornecedores}">
                        <option value="${f.id}" ${produto.ultimoFornecedorId == f.id ? 'selected' : ''}>${f.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.ultimoFornecedorId}"><div class="field-error">${errors.ultimoFornecedorId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorPagoUltimaExecucao">Valor pago na ultima execucao</label>
                <input type="number" id="valorPagoUltimaExecucao" name="valorPagoUltimaExecucao" step="0.01" min="0"
                       value="${produto.valorPagoUltimaExecucao}">
                <c:if test="${not empty errors.valorPagoUltimaExecucao}"><div class="field-error">${errors.valorPagoUltimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="dataAgendadaProximaExecucao">Data agendada da proxima execucao</label>
                <input type="date" id="dataAgendadaProximaExecucao" name="dataAgendadaProximaExecucao"
                       value="${produto.dataAgendadaProximaExecucao}">
                <c:if test="${not empty errors.dataAgendadaProximaExecucao}"><div class="field-error">${errors.dataAgendadaProximaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="fornecedorProximaExecucaoId">Fornecedor da proxima execucao</label>
                <select id="fornecedorProximaExecucaoId" name="fornecedorProximaExecucaoId">
                    <option value="" ${empty produto.fornecedorProximaExecucaoId ? 'selected' : ''}>Nao informado</option>
                    <c:forEach var="f" items="${fornecedores}">
                        <option value="${f.id}" ${produto.fornecedorProximaExecucaoId == f.id ? 'selected' : ''}>${f.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.fornecedorProximaExecucaoId}"><div class="field-error">${errors.fornecedorProximaExecucaoId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorOrcadoProximaExecucao">Valor orcado da proxima execucao</label>
                <input type="number" id="valorOrcadoProximaExecucao" name="valorOrcadoProximaExecucao" step="0.01" min="0"
                       value="${produto.valorOrcadoProximaExecucao}">
                <c:if test="${not empty errors.valorOrcadoProximaExecucao}"><div class="field-error">${errors.valorOrcadoProximaExecucao}</div></c:if>
            </div>

            <button type="submit" class="btn btn-primary">Salvar</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/produtos">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>
    </div>
</div>
</body>
</html>
