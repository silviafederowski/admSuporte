<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="readOnly" value="${sessionScope.userNivel > 9}" />
<c:set var="dis" value="${readOnly ? 'disabled' : ''}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Serviço de manutenção - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${readOnly}">Consultar serviço</c:when>
                    <c:when test="${empty param.id}">Novo serviço</c:when>
                    <c:otherwise>Editar serviço</c:otherwise>
                </c:choose>
            </h1>
            <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
        </div>
        <p class="subtitle">Dados do serviço de manutenção</p>

        <c:if test="${not empty errors.form}">
            <p class="form-error">${errors.form}</p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/servicos/form">
            <input type="hidden" name="id" value="${param.id}">

            <div class="field">
                <label for="descricao">Descrição</label>
                <input type="text" id="descricao" name="descricao" value="${servico.descricao}" ${dis} required>
                <c:if test="${not empty errors.descricao}"><div class="field-error">${errors.descricao}</div></c:if>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="unidadePeriodicidade">Unidade da periodicidade</label>
                    <select id="unidadePeriodicidade" name="unidadePeriodicidade" ${dis} required
                            onchange="atualizarCampoPeriodicidade()">
                        <option value="" disabled ${empty servico.unidadePeriodicidade ? 'selected' : ''}>Selecione...</option>
                        <option value="dia" ${servico.unidadePeriodicidade == 'DIA' ? 'selected' : ''}>Dias</option>
                        <option value="mes" ${servico.unidadePeriodicidade == 'MES' ? 'selected' : ''}>Meses</option>
                        <option value="ano" ${servico.unidadePeriodicidade == 'ANO' ? 'selected' : ''}>Anos</option>
                        <option value="por_demanda" ${servico.unidadePeriodicidade == 'POR_DEMANDA' ? 'selected' : ''}>Por demanda</option>
                    </select>
                    <c:if test="${not empty errors.unidadePeriodicidade}"><div class="field-error">${errors.unidadePeriodicidade}</div></c:if>
                </div>

                <div class="field" id="periodicidadeField">
                    <label for="periodicidade">Periodicidade</label>
                    <input type="number" id="periodicidade" name="periodicidade" min="1" ${dis}
                           value="${not empty param.periodicidade ? param.periodicidade : servico.periodicidade}">
                    <c:if test="${not empty errors.periodicidade}"><div class="field-error">${errors.periodicidade}</div></c:if>
                </div>

                <div class="field">
                    <label for="tipo">Tipo</label>
                    <select id="tipo" name="tipo" ${dis} required>
                        <option value="" disabled ${empty servico.tipo ? 'selected' : ''}>Selecione...</option>
                        <option value="contratado" ${servico.tipo == 'CONTRATADO' ? 'selected' : ''}>Contratado</option>
                        <option value="solicitar" ${servico.tipo == 'SOLICITAR' ? 'selected' : ''}>Solicitar</option>
                    </select>
                    <c:if test="${not empty errors.tipo}"><div class="field-error">${errors.tipo}</div></c:if>
                </div>
            </div>

            <script>
                function atualizarCampoPeriodicidade() {
                    var unidade = document.getElementById('unidadePeriodicidade').value;
                    var campo = document.getElementById('periodicidadeField');
                    var input = document.getElementById('periodicidade');
                    var porDemanda = unidade === 'por_demanda';
                    campo.hidden = porDemanda;
                    input.required = !porDemanda;
                }
                atualizarCampoPeriodicidade();
            </script>

            <div class="field">
                <label for="ultimaExecucao">Data da última execução</label>
                <input type="date" id="ultimaExecucao" name="ultimaExecucao" value="${servico.ultimaExecucao}" ${dis}>
                <c:if test="${not empty errors.ultimaExecucao}"><div class="field-error">${errors.ultimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="ultimoPrestadorId">Prestador da última atualização</label>
                <select id="ultimoPrestadorId" name="ultimoPrestadorId" ${dis}>
                    <option value="" ${empty servico.ultimoPrestadorId ? 'selected' : ''}>Não informado</option>
                    <c:forEach var="p" items="${prestadores}">
                        <option value="${p.id}" ${servico.ultimoPrestadorId == p.id ? 'selected' : ''}>${p.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.ultimoPrestadorId}"><div class="field-error">${errors.ultimoPrestadorId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorPagoUltimaExecucao">Valor pago na última execução</label>
                <input type="number" id="valorPagoUltimaExecucao" name="valorPagoUltimaExecucao" step="0.01" min="0" ${dis}
                       value="${servico.valorPagoUltimaExecucao}">
                <c:if test="${not empty errors.valorPagoUltimaExecucao}"><div class="field-error">${errors.valorPagoUltimaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="dataAgendadaProximaExecucao">Data agendada da próxima execução</label>
                <input type="date" id="dataAgendadaProximaExecucao" name="dataAgendadaProximaExecucao" ${dis}
                       value="${servico.dataAgendadaProximaExecucao}">
                <c:if test="${not empty errors.dataAgendadaProximaExecucao}"><div class="field-error">${errors.dataAgendadaProximaExecucao}</div></c:if>
            </div>

            <div class="field">
                <label for="prestadorProximaExecucaoId">Prestador da próxima execução</label>
                <select id="prestadorProximaExecucaoId" name="prestadorProximaExecucaoId" ${dis}>
                    <option value="" ${empty servico.prestadorProximaExecucaoId ? 'selected' : ''}>Não informado</option>
                    <c:forEach var="p" items="${prestadores}">
                        <option value="${p.id}" ${servico.prestadorProximaExecucaoId == p.id ? 'selected' : ''}>${p.nomeRazaoSocial}</option>
                    </c:forEach>
                </select>
                <c:if test="${not empty errors.prestadorProximaExecucaoId}"><div class="field-error">${errors.prestadorProximaExecucaoId}</div></c:if>
            </div>

            <div class="field">
                <label for="valorOrcadoProximaExecucao">Valor orçado da próxima execução</label>
                <input type="number" id="valorOrcadoProximaExecucao" name="valorOrcadoProximaExecucao" step="0.01" min="0" ${dis}
                       value="${servico.valorOrcadoProximaExecucao}">
                <c:if test="${not empty errors.valorOrcadoProximaExecucao}"><div class="field-error">${errors.valorOrcadoProximaExecucao}</div></c:if>
            </div>

            <c:if test="${!readOnly}">
                <button type="submit" class="btn btn-primary">Salvar</button>
            </c:if>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/servicos">Cancelar</a>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
        </form>

        <c:if test="${!readOnly && not empty param.id}">
            <form method="post" action="${pageContext.request.contextPath}/servicos/excluir"
                  onsubmit="return confirm('Excluir este serviço?');">
                <input type="hidden" name="id" value="${param.id}">
                <button type="submit" class="btn btn-danger">Excluir</button>
            </form>
        </c:if>
    </div>
</div>
</body>
</html>
