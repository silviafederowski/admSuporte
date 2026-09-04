<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "prestadores") --%>
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Prestador - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar prestador</c:when>
                    <c:when test="${empty param.id}">Novo prestador</c:when>
                    <c:otherwise>Editar prestador</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">Dados do prestador de serviço</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/prestadores/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="nomeRazaoSocial">Nome ou razão social</label>
                <input type="text" id="nomeRazaoSocial" name="nomeRazaoSocial" value="${prestador.nomeRazaoSocial}" ${dis} required>
                <c:if test="${not empty errors.nomeRazaoSocial}"><div class="field-error">${errors.nomeRazaoSocial}</div></c:if>
            </div>

            <div class="field">
                <label for="email">Email</label>
                <textarea id="email" name="email" placeholder="Um ou mais e-mails, um por linha" ${dis}>${prestador.email}</textarea>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato1Nome">Contato 1 - nome</label>
                    <input type="text" id="contato1Nome" name="contato1Nome" value="${prestador.contato1Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato1Cargo">Cargo</label>
                    <input type="text" id="contato1Cargo" name="contato1Cargo" value="${prestador.contato1Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato1Telefone">Telefone</label>
                    <input type="text" id="contato1Telefone" name="contato1Telefone" value="${prestador.contato1Telefone}" ${dis}>
                </div>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato2Nome">Contato 2 - nome</label>
                    <input type="text" id="contato2Nome" name="contato2Nome" value="${prestador.contato2Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato2Cargo">Cargo</label>
                    <input type="text" id="contato2Cargo" name="contato2Cargo" value="${prestador.contato2Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato2Telefone">Telefone</label>
                    <input type="text" id="contato2Telefone" name="contato2Telefone" value="${prestador.contato2Telefone}" ${dis}>
                </div>
            </div>

            <div class="contact-row">
                <div class="field">
                    <label for="contato3Nome">Contato 3 - nome</label>
                    <input type="text" id="contato3Nome" name="contato3Nome" value="${prestador.contato3Nome}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato3Cargo">Cargo</label>
                    <input type="text" id="contato3Cargo" name="contato3Cargo" value="${prestador.contato3Cargo}" ${dis}>
                </div>
                <div class="field">
                    <label for="contato3Telefone">Telefone</label>
                    <input type="text" id="contato3Telefone" name="contato3Telefone" value="${prestador.contato3Telefone}" ${dis}>
                </div>
            </div>

            <div class="field">
                <label for="classificacao">Classificação</label>
                <select id="classificacao" name="classificacao" ${dis} required>
                    <option value="nao_avaliado" ${empty prestador.classificacao || prestador.classificacao == 'NAO_AVALIADO' ? 'selected' : ''}>Não avaliado</option>
                    <option value="muito_bom" ${prestador.classificacao == 'MUITO_BOM' ? 'selected' : ''}>Muito bom</option>
                    <option value="bom" ${prestador.classificacao == 'BOM' ? 'selected' : ''}>Bom</option>
                    <option value="medio" ${prestador.classificacao == 'MEDIO' ? 'selected' : ''}>Médio</option>
                    <option value="ruim" ${prestador.classificacao == 'RUIM' ? 'selected' : ''}>Ruim</option>
                    <option value="muito_ruim" ${prestador.classificacao == 'MUITO_RUIM' ? 'selected' : ''}>Muito ruim</option>
                </select>
                <c:if test="${not empty errors.classificacao}"><div class="field-error">${errors.classificacao}</div></c:if>
            </div>

            <div class="field">
                <label for="tipo">Tipo</label>
                <select id="tipo" name="tipo" ${dis} required>
                    <option value="" disabled ${empty prestador.tipo ? 'selected' : ''}>Selecione...</option>
                    <option value="contratado" ${prestador.tipo == 'CONTRATADO' ? 'selected' : ''}>Contratado</option>
                    <option value="solicitar" ${prestador.tipo == 'SOLICITAR' ? 'selected' : ''}>Solicitar</option>
                </select>
                <c:if test="${not empty errors.tipo}"><div class="field-error">${errors.tipo}</div></c:if>
            </div>

            <div class="field">
                <label>Serviços realizados por este prestador</label>
                <c:choose>
                    <c:when test="${empty servicos}">
                        <p class="empty-state">Nenhum serviço cadastrado ainda.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="row-list">
                            <c:forEach var="servico" items="${servicos}">
                                <label class="row-item">
                                    <input type="checkbox" name="servicoIds" value="${servico.id}" ${dis}
                                           ${prestador.servicoIds.contains(servico.id) ? 'checked' : ''}>
                                    <span>${servico.descricao}</span>
                                </label>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="field">
                <label for="observacao">Observação</label>
                <textarea id="observacao" name="observacao" style="min-height:160px;" ${dis}>${prestador.observacao}</textarea>
            </div>

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/prestadores">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form method="post" action="${pageContext.request.contextPath}/prestadores/excluir"
                  onsubmit="return confirm('Excluir este prestador?');">
                <input type="hidden" name="id" value="${param.id}">
                <button type="submit" class="btn btn-danger">Excluir</button>
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
