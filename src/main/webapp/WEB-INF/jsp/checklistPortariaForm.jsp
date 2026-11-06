<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- readOnly vem de request (definido pelo MenuAutorizacaoFilter, tela "checklist-portaria").
     No checklist gravado, podeEditar (servlet) e false para o colega que so verifica e depois da
     verificacao, quando o checklist fica travado para todos. --%>
<c:set var="editavel" value="${!readOnly && (novo || podeEditar)}" />
<c:set var="dis" value="${editavel ? '' : 'disabled'}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Checklist - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=38">
</head>
<body>
<div class="page">
    <div class="card card-wide card-checklist">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <h1 class="title" style="text-align:left;margin:0;">
                <c:choose>
                    <c:when test="${novo}">Novo checklist</c:when>
                    <c:when test="${modoValidacao}">Validar checklist</c:when>
                    <c:when test="${readOnly}">Consultar checklist</c:when>
                    <c:otherwise>Checklist</c:otherwise>
                </c:choose>
            </h1>
            <div class="toolbar-actions">
                <c:if test="${editavel && not empty itens}">
                    <button type="submit" id="botaoSalvar" form="checklistForm" class="menu-icon-link" title="Salvar" aria-label="Salvar">💾</button>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/checklist-portaria" title="Cancelar" aria-label="Cancelar">↩️</a>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">
                ${formError}
                <c:if test="${not empty dataExistente}">
                    <c:url var="urlDataExistente" value="/checklist-portaria/form">
                        <c:param name="data" value="${dataExistente}" />
                        <c:param name="usuario" value="${usuarioChecklist}" />
                    </c:url>
                    <br><a class="link" href="${urlDataExistente}">Abrir o checklist existente para alterar</a>
                </c:if>
            </p>
        </c:if>
        <c:if test="${novo}">
            <%-- Aviso imediato ao escolher uma data que ja tem checklist (ver script no fim). --%>
            <c:url var="urlChecklistUsuario" value="/checklist-portaria/form">
                <c:param name="usuario" value="${usuarioChecklist}" />
            </c:url>
            <p id="avisoDataExistente" class="form-error" hidden>
                Já existe um checklist seu nesta data. Não é possível incluir outro: altere o checklist que já existe.
                <br><a id="linkDataExistente" class="link" href="#">Abrir o checklist existente para alterar</a>
            </p>
        </c:if>

        <form id="checklistForm" method="post" action="${pageContext.request.contextPath}/checklist-portaria/form">
            <input type="hidden" name="modo" value="${novo ? 'novo' : 'editar'}">

            <%-- Uma linha: quem fez (data, hora, usuario) a esquerda; status e validacao a direita.
                 Os campos da validacao pertencem ao formulario "validarForm" (atributo form), fora deste. --%>
            <div class="checklist-topo">
                <div class="checklist-topo-lado">
                    <label for="data">Data</label>
                    <c:choose>
                        <c:when test="${novo}">
                            <input type="date" id="data" name="data" value="${data}" class="topo-data" ${dis} required>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="data" value="${data}">
                            <input type="text" id="data" value="${dataFormatada}" class="topo-curto" disabled>
                        </c:otherwise>
                    </c:choose>
                    <label for="hora">Hora</label>
                    <c:choose>
                        <c:when test="${novo}">
                            <input type="time" id="hora" name="hora" value="${hora}" class="topo-hora" ${dis} required>
                        </c:when>
                        <c:otherwise>
                            <input type="text" id="hora" value="${hora}" class="topo-curto" disabled>
                        </c:otherwise>
                    </c:choose>
                    <label for="usuario">Usuário</label>
                    <%-- So exibido: no checklist novo o servidor usa sempre o usuario logado. --%>
                    <input type="text" id="usuario" value="${usuarioChecklist}" class="topo-nome" disabled>
                    <c:if test="${!novo}"><input type="hidden" name="usuario" value="${usuarioChecklist}"></c:if>
                </div>

                <%-- Status e validacao. Status: o autor escolhe "em andamento" ou "disponivel para validacao";
                     "validada" so pela validacao (tela aberta pelo icone da lista), e ai nada mais muda.
                     Quem valida e quando sao preenchidos sozinhos (usuario logado e o momento do registro). --%>
                <div class="checklist-topo-lado checklist-topo-verificacao">
                    <label for="status">Status</label>
                    <c:choose>
                        <c:when test="${editavel}">
                            <c:set var="statusAtual" value="${not empty param.status ? param.status : cabecalho.status}" />
                            <select id="status" name="status" class="topo-status">
                                <option value="em_andamento" ${statusAtual != 'disponivel' ? 'selected' : ''}>Em andamento</option>
                                <option value="disponivel" ${statusAtual == 'disponivel' ? 'selected' : ''}>Disponível para validação</option>
                            </select>
                        </c:when>
                        <c:otherwise>
                            <span class="badge ${cabecalho.verificado ? 'badge-resolvido' : 'badge-pendente'}">${empty cabecalho ? 'Em andamento' : cabecalho.statusLabel}</span>
                        </c:otherwise>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!novo && cabecalho.verificado}">
                            <span>por <strong><c:out value="${cabecalho.verificadoPor}" /></strong> em ${cabecalho.verificadoEmFormatado}</span>
                            <c:if test="${not empty cabecalho.verificacaoObs}">
                                <input type="text" class="topo-obs" value="<c:out value='${cabecalho.verificacaoObs}' />"
                                       title="<c:out value='${cabecalho.verificacaoObs}' />" disabled>
                            </c:if>
                        </c:when>
                        <c:when test="${modoValidacao}">
                            <span>Validação por <strong><c:out value="${sessionScope.userName}" /></strong>
                                <span class="field-hint" title="Data e hora gravadas no momento em que concluir">agora</span></span>
                            <input type="text" id="verificacaoObs" name="verificacaoObs" form="validarForm" maxlength="1000"
                                   class="topo-obs" placeholder="Observação geral (opcional)" value="<c:out value='${verificacaoObsDigitada}' />">
                            <button type="submit" form="validarForm" class="btn btn-primary btn-small">Concluir validação</button>
                        </c:when>
                    </c:choose>
                </div>
            </div>

            <%-- Duas colunas: itens checaveis a esquerda; encomendas (quantidades e tabela) a direita.
                 Em tela estreita, uma embaixo da outra (ver .checklist-colunas no style.css). --%>
            <div class="checklist-colunas">
            <div class="checklist-col">
            <c:choose>
                <c:when test="${empty itens}">
                    <p class="empty-state">Não há itens de checklist para o seu tipo de usuário.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th style="width:40px;" title="Checado">OK</th>
                                <th style="text-align:left;">Item</th>
                                <th style="text-align:left;min-width:150px;">Observação</th>
                                <c:if test="${!novo}"><th style="width:90px;">Alterado em</th></c:if>
                                <%-- Validacao item a item: so na tela de validacao e depois de validado. --%>
                                <c:set var="mostraValidacao" value="${!novo && (modoValidacao || cabecalho.verificado)}" />
                                <c:if test="${mostraValidacao}">
                                    <th style="width:44px;" title="Confirmado na validação">Valid.</th>
                                    <th style="text-align:left;min-width:150px;">Obs. da validação</th>
                                </c:if>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="i" items="${itens}">
                                <%-- Clique na linha marca/desmarca, menos dentro da caixa ou da observacao;
                                     o evento "change" aplica a regra checado x observacao (script no fim). --%>
                                <tr class="linha-item" onclick="if (event.target.tagName !== 'INPUT' && event.target.tagName !== 'TEXTAREA') { var cb = this.querySelector('input[name=checado]'); if (!cb.disabled) { cb.checked = !cb.checked; cb.dispatchEvent(new Event('change')); } }"
                                    style="cursor:pointer;">
                                    <td>
                                        <%-- Checklist novo: valor e o item; existente: valor e a linha gravada. --%>
                                        <input type="checkbox" name="checado" value="${novo ? i.itemId : i.id}"
                                               style="width:18px;height:18px;accent-color:var(--primary);"
                                               ${i.checado ? 'checked' : ''} ${dis}>
                                    </td>
                                    <td style="text-align:left;">${i.itemDescricao}</td>
                                    <td style="text-align:left;">
                                        <textarea name="obs_${novo ? i.itemId : i.id}" rows="1" maxlength="1000"
                                                  placeholder="Observação (opcional)"
                                                  style="width:100%;min-height:0;resize:vertical;" ${dis}><c:out value="${i.observacao}" /></textarea>
                                    </td>
                                    <c:if test="${!novo}"><td>${empty i.alteradoEmFormatado ? '-' : i.alteradoEmFormatado}</td></c:if>
                                    <c:if test="${mostraValidacao}">
                                        <%-- Campos do formulario "validarForm" (atributo form); confirmado OU motivo. --%>
                                        <td class="validacao-item">
                                            <input type="checkbox" name="val" value="${i.id}" form="validarForm"
                                                   style="width:18px;height:18px;accent-color:var(--primary);"
                                                   ${i.validado ? 'checked' : ''} ${modoValidacao ? '' : 'disabled'}>
                                        </td>
                                        <td style="text-align:left;">
                                            <textarea name="valobs_${i.id}" rows="1" maxlength="1000" form="validarForm"
                                                      placeholder="Motivo, se não confirmar"
                                                      style="width:100%;min-height:0;resize:vertical;"
                                                      ${modoValidacao ? '' : 'disabled'}><c:out value="${i.validacaoObs}" /></textarea>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
            </div>

            <div class="checklist-col">
                <%-- Rotulos e campos na mesma linha; ate 3 digitos. A soma fica em vermelho enquanto
                     for diferente das encomendas sem baixa no app (script no fim). --%>
                <%-- Uma linha so: "Encomendas - s/baixa no app [qtde] no estoque [soma]". --%>
                <div class="checklist-quantidades">
                    <strong>Encomendas -</strong>
                    <label for="encomendasSemBaixa">s/baixa no app</label>
                    <input type="number" id="encomendasSemBaixa" name="encomendasSemBaixa" min="0" max="999" step="1"
                           inputmode="numeric" value="${encomendas}" placeholder="0" ${dis} required>
                    <label for="somaEstoque" title="Soma das quantidades por unidade, abaixo">no estoque</label>
                    <input type="text" id="somaEstoque" value="0" readonly tabindex="-1"
                           title="Soma das quantidades por unidade, abaixo">
                </div>

                <%-- Encomendas em estoque por unidade: vazio = 0. --%>
                <div class="checklist-estoque-grade">
                    <c:forEach var="u" items="${unidades}">
                        <c:set var="chaveTexto">${u.chave}</c:set>
                        <label>
                            <span>${u.codigo}</span>
                            <input type="number" class="estoque-unidade" name="estoque_${u.chave}" min="0" max="999" step="1"
                                   inputmode="numeric" value="${estoqueValores[chaveTexto]}" placeholder="0" ${dis}>
                        </label>
                    </c:forEach>
                </div>
            </div>
            </div>
        </form>

        <%-- Formulario da validacao: os campos ficam no topo e nas linhas dos itens (atributo form="validarForm"). --%>
        <c:if test="${modoValidacao && !readOnly}">
            <form id="validarForm" method="post" action="${pageContext.request.contextPath}/checklist-portaria/form"
                  onsubmit="return confirm('Depois de validado, o checklist não poderá mais ser alterado. Concluir a validação?');">
                <input type="hidden" name="modo" value="validar">
                <input type="hidden" name="data" value="${data}">
                <input type="hidden" name="usuario" value="${usuarioChecklist}">
            </form>
        </c:if>
    </div>
</div>
<script>
    /* Soma das encomendas em estoque por unidade, ao lado das encomendas sem baixa no app;
       em vermelho enquanto as duas quantidades forem diferentes. */
    (function () {
        var campos = document.querySelectorAll('input.estoque-unidade');
        var soma = document.getElementById('somaEstoque');
        var app = document.getElementById('encomendasSemBaixa');
        function somar() {
            var total = 0;
            campos.forEach(function (c) {
                var n = parseInt(c.value, 10);
                if (!isNaN(n) && n > 0) {
                    total += n;
                }
            });
            soma.value = total;
            var noApp = parseInt(app.value, 10);
            soma.classList.toggle('estoque-diferente', isNaN(noApp) || noApp !== total);
        }
        campos.forEach(function (c) {
            c.addEventListener('input', somar);
        });
        app.addEventListener('input', somar);
        somar();
    })();
</script>
<c:if test="${modoValidacao}">
<script>
    /* Validacao: em cada item, confirmado OU motivo de nao confirmar (mesma regra do servidor). */
    document.querySelectorAll('tr.linha-item').forEach(function (tr) {
        var cb = tr.querySelector('input[name="val"]');
        var obs = tr.querySelector('textarea[name^="valobs_"]');
        if (!cb || !obs) {
            return;
        }
        function aplicar() {
            if (cb.checked) {
                obs.value = '';
            }
            obs.disabled = cb.checked;
        }
        cb.addEventListener('change', aplicar);
        obs.addEventListener('input', function () {
            if (obs.value.trim() !== '' && cb.checked) {
                cb.checked = false;
                aplicar();
            }
        });
        aplicar();
    });
</script>
</c:if>
<c:if test="${editavel}">
<script>
    /* Em cada linha: item checado nao tem observacao (apaga e bloqueia o campo); digitar uma
       observacao desfaz a checagem. O servidor aplica a mesma regra ao gravar. */
    document.querySelectorAll('tr.linha-item').forEach(function (tr) {
        var cb = tr.querySelector('input[name="checado"]');
        var obs = tr.querySelector('textarea');
        if (!cb || !obs) {
            return;
        }
        function aplicarChecado() {
            if (cb.checked) {
                obs.value = '';
            }
            obs.disabled = cb.checked;
            obs.placeholder = cb.checked ? 'Item checado: sem observação' : 'Observação (opcional)';
        }
        cb.addEventListener('change', aplicarChecado);
        obs.addEventListener('input', function () {
            if (obs.value.trim() !== '' && cb.checked) {
                cb.checked = false;
                aplicarChecado();
            }
        });
        aplicarChecado();
    });
</script>
</c:if>
<c:if test="${novo && !readOnly}">
<script>
    /* Ao marcar/desmarcar o primeiro item, data e hora ficam travadas. readOnly (e nao disabled),
       para que continuem sendo enviadas no formulario. */
    (function () {
        var data = document.getElementById('data');
        var hora = document.getElementById('hora');
        function travarDataHora() {
            data.readOnly = true;
            hora.readOnly = true;
            data.style.opacity = hora.style.opacity = '0.6';
        }
        document.querySelectorAll('input[name="checado"]').forEach(function (cb) {
            cb.addEventListener('change', travarDataHora);
        });
        // Clique na linha marca a caixa via script, o que nao dispara "change": trava tambem.
        document.querySelectorAll('#checklistForm tbody tr').forEach(function (tr) {
            tr.addEventListener('click', travarDataHora);
        });
    })();

    /* Data que ja tem checklist do usuario: avisa, mostra o link para o existente e bloqueia o salvar.
       O servidor confere de novo ao gravar. */
    (function () {
        var existentes = '${datasExistentes}'.split(',').filter(Boolean);
        var data = document.getElementById('data');
        var aviso = document.getElementById('avisoDataExistente');
        var link = document.getElementById('linkDataExistente');
        var salvar = document.getElementById('botaoSalvar');
        function conferirData() {
            var repetida = existentes.indexOf(data.value) !== -1;
            aviso.hidden = !repetida;
            link.href = '${urlChecklistUsuario}' + '&data=' + encodeURIComponent(data.value);
            if (salvar) {
                salvar.disabled = repetida;
                salvar.style.opacity = repetida ? '0.4' : '';
            }
        }
        data.addEventListener('change', conferirData);
        data.addEventListener('input', conferirData);
        conferirData();
    })();
</script>
</c:if>
</body>
</html>
