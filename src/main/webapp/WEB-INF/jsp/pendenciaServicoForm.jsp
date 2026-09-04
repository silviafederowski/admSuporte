<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "pendencias-servico") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Registro do histórico - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar registro</c:when>
                    <c:when test="${empty param.id}">Novo registro</c:when>
                    <c:otherwise>Editar registro</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">Dados do registro do histórico de serviço</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/pendencias-servico/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="servicoId">Serviço</label>
                <select id="servicoId" name="servicoId" ${dis} required>
                    <option value="" disabled ${empty pendencia.servicoId ? 'selected' : ''}>Selecione...</option>
                    <c:forEach var="s" items="${servicos}">
                        <option value="${s.id}" ${pendencia.servicoId == s.id ? 'selected' : ''}>${s.descricao}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.servicoId}"><div class="field-error">${errors.servicoId}</div></c:if>
            </div>

            <div class="field">
                <label for="prestadorId">Prestador</label>
                <select id="prestadorId" name="prestadorId" ${dis} required>
                    <option value="" disabled ${empty pendencia.prestadorId ? 'selected' : ''}>Selecione...</option>
                    <c:forEach var="p" items="${prestadores}">
                        <option value="${p.id}" ${pendencia.prestadorId == p.id ? 'selected' : ''}>${p.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.prestadorId}"><div class="field-error">${errors.prestadorId}</div></c:if>
            </div>

            <div class="field">
                <label for="data">Data</label>
                <input type="date" id="data" name="data" value="${pendencia.data}" ${dis} required>
                <c:if test="${not empty errors.data}"><div class="field-error">${errors.data}</div></c:if>
            </div>

            <div class="field">
                <label for="nomeTecnico">Nome do técnico</label>
                <input type="text" id="nomeTecnico" name="nomeTecnico" value="${pendencia.nomeTecnico}" ${dis} required>
                <c:if test="${not empty errors.nomeTecnico}"><div class="field-error">${errors.nomeTecnico}</div></c:if>
            </div>

            <div class="field">
                <label for="status">Situação</label>
                <select id="status" name="status" ${dis} required>
                    <option value="" disabled ${empty pendencia.status ? 'selected' : ''}>Selecione...</option>
                    <option value="pendente" ${pendencia.status == 'PENDENTE' ? 'selected' : ''}>Pendente</option>
                    <option value="resolvido" ${pendencia.status == 'RESOLVIDO' ? 'selected' : ''}>Resolvido</option>
                </select>
                <c:if test="${not empty errors.status}"><div class="field-error">${errors.status}</div></c:if>
            </div>

            <div class="field">
                <label for="descricaoOcorrencia">Descrição da ocorrência</label>
                <textarea id="descricaoOcorrencia" name="descricaoOcorrencia" style="min-height:160px;" ${dis} required>${pendencia.descricaoOcorrencia}</textarea>
                <c:if test="${not empty errors.descricaoOcorrencia}"><div class="field-error">${errors.descricaoOcorrencia}</div></c:if>
            </div>

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/pendencias-servico">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form method="post" action="${pageContext.request.contextPath}/pendencias-servico/excluir"
                  onsubmit="return confirm('Excluir este registro?');">
                <input type="hidden" name="id" value="${param.id}">
                <button type="submit" class="btn btn-danger">Excluir</button>
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
