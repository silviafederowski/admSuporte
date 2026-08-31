<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Servico de manutencao - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <h1 class="title">${empty param.id ? 'Novo servico' : 'Editar servico'}</h1>
        <p class="subtitle">Dados do servico de manutencao</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/servicos/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="descricao">Descricao</label>
                <input type="text" id="descricao" name="descricao" value="${servico.descricao}" required>
                <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
            </div>

            <div class="field">
                <label for="periodicidade">Periodicidade</label>
                <input type="number" id="periodicidade" name="periodicidade" min="1"
                       value="${not empty param.periodicidade ? param.periodicidade : servico.periodicidade}" required>
                <c:if test="${not empty errors.periodicidade}"><div class="field-error">${errors.periodicidade}</div></c:if>
            </div>

            <div class="field">
                <label for="unidadePeriodicidade">Unidade da periodicidade</label>
                <select id="unidadePeriodicidade" name="unidadePeriodicidade" required>
                    <option value="" disabled ${empty servico.unidadePeriodicidade ? 'selected' : ''}>Selecione...</option>
                    <option value="dia" ${servico.unidadePeriodicidade == 'DIA' ? 'selected' : ''}>Dias</option>
                    <option value="mes" ${servico.unidadePeriodicidade == 'MES' ? 'selected' : ''}>Meses</option>
                    <option value="ano" ${servico.unidadePeriodicidade == 'ANO' ? 'selected' : ''}>Anos</option>
                </select>
                <c:if test="${not empty errors.unidadePeriodicidade}"><div class="field-error">${errors.unidadePeriodicidade}</div></c:if>
            </div>

            <div class="field">
                <label for="ultimaExecucao">Data da ultima execucao</label>
                <input type="date" id="ultimaExecucao" name="ultimaExecucao" value="${servico.ultimaExecucao}">
                <c:if test="${not empty errors.ultimaExecucao}"><div class="field-error">${errors.ultimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="ultimoPrestadorId">Prestador da ultima atualizacao</label>
                <select id="ultimoPrestadorId" name="ultimoPrestadorId">
                    <option value="" ${empty servico.ultimoPrestadorId ? 'selected' : ''}>Nao informado</option>
                    <c:forEach var="p" items="${prestadores}">
                        <option value="${p.id}" ${servico.ultimoPrestadorId == p.id ? 'selected' : ''}>${p.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.ultimoPrestadorId}"><div class="field-error">${errors.ultimoPrestadorId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorPagoUltimaExecucao">Valor pago na ultima execucao</label>
                <input type="number" id="valorPagoUltimaExecucao" name="valorPagoUltimaExecucao" step="0.01" min="0"
                       value="${servico.valorPagoUltimaExecucao}">
                <c:if test="${not empty errors.valorPagoUltimaExecucao}"><div class="field-error">${errors.valorPagoUltimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="dataAgendadaProximaExecucao">Data agendada da proxima execucao</label>
                <input type="date" id="dataAgendadaProximaExecucao" name="dataAgendadaProximaExecucao"
                       value="${servico.dataAgendadaProximaExecucao}">
                <c:if test="${not empty errors.dataAgendadaProximaExecucao}"><div class="field-error">${errors.dataAgendadaProximaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="prestadorProximaExecucaoId">Prestador da proxima execucao</label>
                <select id="prestadorProximaExecucaoId" name="prestadorProximaExecucaoId">
                    <option value="" ${empty servico.prestadorProximaExecucaoId ? 'selected' : ''}>Nao informado</option>
                    <c:forEach var="p" items="${prestadores}">
                        <option value="${p.id}" ${servico.prestadorProximaExecucaoId == p.id ? 'selected' : ''}>${p.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.prestadorProximaExecucaoId}"><div class="field-error">${errors.prestadorProximaExecucaoId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorOrcadoProximaExecucao">Valor orcado da proxima execucao</label>
                <input type="number" id="valorOrcadoProximaExecucao" name="valorOrcadoProximaExecucao" step="0.01" min="0"
                       value="${servico.valorOrcadoProximaExecucao}">
                <c:if test="${not empty errors.valorOrcadoProximaExecucao}"><div class="field-error">${errors.valorOrcadoProximaExecucao}</div></c:if>
            </div>

            <button type="submit" class="btn btn-primary">Salvar</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/servicos">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>
    </div>
</div>
</body>
</html>
