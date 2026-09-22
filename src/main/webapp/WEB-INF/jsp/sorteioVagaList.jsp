<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sorteio vagas - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=30">
</head>
<body>
<div class="page">
    <div class="card card-sorteio">
        <p class="condominio-nome" style="text-align:left;">${nomeCondominio}</p>
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Sorteio vagas</h1>
            </div>
            <div class="sorteio-legenda sorteio-legenda-toolbar">
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-interna"></span> Interna</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-borda-individual"></span> Individual (borda preta)</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-externa"></span> Externa</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch"></span> Dupla (sem borda)</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-preferencial"></span> Preferencial (idosos)</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-emergencia"></span> Emergência</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-zelador"></span> Zelador</span>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="javascript:void(0)" onclick="iniciarSorteioIdoso()" title="Sortear vaga de idoso" aria-label="Sortear vaga de idoso">🎲</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <div class="sorteio-colunas">
            <div class="sorteio-coluna sorteio-coluna-dividida">
                <p class="sorteio-secao-titulo-principal">Vagas por tipo</p>
                <div class="sorteio-coluna-corpo sorteio-coluna-corpo-distribuido">
                    <div class="sorteio-secao">
                        <p class="sorteio-secao-titulo">Andar -2</p>
                        <div class="sorteio-vagas-flow">
                            <c:forEach var="celula" items="${vagasAndarMenos2}">
                                <span class="sorteio-vaga-chip ${celula.cssClass}" data-vaga="${celula.codigos}">${celula.codigos}</span>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="sorteio-secao">
                        <p class="sorteio-secao-titulo">Andar -1</p>
                        <div class="sorteio-vagas-flow">
                            <c:forEach var="celula" items="${vagasAndarMenos1}">
                                <span class="sorteio-vaga-chip ${celula.cssClass}" data-vaga="${celula.codigos}">${celula.codigos}</span>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="sorteio-secao">
                        <p class="sorteio-secao-titulo">Andar 0</p>
                        <div class="sorteio-vagas-flow">
                            <c:forEach var="celula" items="${vagasAndar0}">
                                <span class="sorteio-vaga-chip ${celula.cssClass}" data-vaga="${celula.codigos}">${celula.codigos}</span>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>

            <div class="sorteio-coluna">
                <p class="sorteio-secao-titulo-principal">
                    Posição em <c:choose><c:when test="${not empty ano}">${ano}</c:when><c:otherwise>-</c:otherwise></c:choose>
                </p>
                <div class="sorteio-coluna-corpo">
                    <c:choose>
                        <c:when test="${empty grade}">
                            <p class="empty-state">Nenhum registro encontrado em histórico de vagas.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table sorteio-table">
                                    <thead>
                                    <tr>
                                        <th>Andar</th>
                                        <th>Finais 1</th>
                                        <th>Finais 2</th>
                                        <th>Finais 3</th>
                                        <th>Finais 4</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="linha" items="${grade}">
                                        <tr>
                                            <td>${linha.key}º</td>
                                            <c:forEach var="celula" items="${linha.value}">
                                                <td class="${celula.cssClass}" data-unidade-posicao="${celula.unidadeCodigo}">${celula.codigos}</td>
                                            </c:forEach>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="sorteio-coluna">
                <p class="sorteio-secao-titulo-principal">Sorteio</p>
                <div class="sorteio-coluna-corpo" id="sorteioPainel">
                    <p class="empty-state" id="sorteioPlaceholder">Clique no ícone 🎲 para sortear a vaga de idoso.</p>
                </div>
            </div>

            <div class="sorteio-coluna">
                <p class="sorteio-secao-titulo-principal">
                    Prévia para <c:choose><c:when test="${not empty anoSeguinte}">${anoSeguinte}</c:when><c:otherwise>-</c:otherwise></c:choose>
                </p>
                <div class="sorteio-coluna-corpo">
                    <c:choose>
                        <c:when test="${empty gradePrevia}">
                            <p class="empty-state">Nenhum registro encontrado em histórico de vagas.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table sorteio-table">
                                    <thead>
                                    <tr>
                                        <th>Andar</th>
                                        <th>Finais 1</th>
                                        <th>Finais 2</th>
                                        <th>Finais 3</th>
                                        <th>Finais 4</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="linha" items="${gradePrevia}">
                                        <tr>
                                            <td>${linha.key}º</td>
                                            <c:forEach var="celula" items="${linha.value}">
                                                <td class="${celula.cssClass}" data-unidade-previa="${celula.codigos}">${celula.codigos}</td>
                                            </c:forEach>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

    </div>
</div>
<script>
    var sorteioCandidatos = [
        <c:forEach var="cand" items="${candidatasIdoso}" varStatus="st">
        { codigo: "${cand.codigo}", vagasGaragem: ${cand.vagasGaragem} }<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioVagasIdoso = [
        <c:forEach var="vaga" items="${vagasIdoso}" varStatus="st">
        "${vaga}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioVagasInternasIndividuais = [
        <c:forEach var="vaga" items="${vagasInternasIndividuais}" varStatus="st">
        "${vaga}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioVagasInternasDuplas = [
        <c:forEach var="vaga" items="${vagasInternasDuplas}" varStatus="st">
        "${vaga}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioVagasExternasIndividuais = [
        <c:forEach var="vaga" items="${vagasExternasIndividuais}" varStatus="st">
        "${vaga}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioVagasExternasDuplas = [
        <c:forEach var="vaga" items="${vagasExternasDuplas}" varStatus="st">
        "${vaga}"<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    ];
    var sorteioUnidadesVagasGaragem = {
        <c:forEach var="u" items="${todasUnidades}" varStatus="st">
        "${u.codigo}": ${u.vagasGaragem}<c:if test="${!st.last}">,</c:if>
        </c:forEach>
    };

    var sorteioEmAndamento = false;

    function embaralhar(array) {
        for (var i = array.length - 1; i > 0; i--) {
            var j = Math.floor(Math.random() * (i + 1));
            var tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    function sorteioAdicionarLinha(texto) {
        var painel = document.getElementById('sorteioPainel');
        var linha = document.createElement('p');
        linha.className = 'sorteio-texto-live';
        linha.textContent = texto;
        painel.appendChild(linha);
        return linha;
    }

    // ---- Fase 1: sorteio da vaga de idoso ----
    function iniciarSorteioIdoso() {
        if (sorteioEmAndamento) {
            return;
        }
        sorteioEmAndamento = true;
        var painel = document.getElementById('sorteioPainel');
        painel.innerHTML = '<p class="sorteio-secao-titulo">Fase 1 — Sorteio da vaga de idoso</p>';

        if (!sorteioCandidatos.length) {
            sorteioAdicionarLinha('Nenhuma unidade candidata a vaga de idoso.');
            sorteioAdicionarConfirmar('Prosseguir para a fase 2?', function () {
                iniciarSorteioGeral(null);
            });
            return;
        }

        var textoEl = sorteioAdicionarLinha('Sorteando unidade para vaga de idoso: ');
        var codigosRevelados = [];
        var indice = 0;

        function revelarProxima() {
            codigosRevelados.push(sorteioCandidatos[indice].codigo);
            var ultimaDaVez = indice === sorteioCandidatos.length - 1;
            textoEl.textContent = 'Sorteando unidade para vaga de idoso: '
                    + codigosRevelados.join(', ') + (ultimaDaVez ? '' : ' ...');
            indice++;
            if (indice < sorteioCandidatos.length) {
                setTimeout(revelarProxima, 700);
            } else {
                setTimeout(mostrarResultadoIdoso, 700);
            }
        }

        function mostrarResultadoIdoso() {
            var vencedor = sorteioCandidatos[sorteioCandidatos.length - 1];
            var vagaAtribuida = (vencedor.vagasGaragem >= 2 && sorteioVagasIdoso.length >= 2)
                    ? (sorteioVagasIdoso[0] + '|' + sorteioVagasIdoso[1])
                    : sorteioVagasIdoso[0];

            sorteioAdicionarLinha('Unidade sorteada: ' + vencedor.codigo + ' — vaga(s): ' + vagaAtribuida);
            sorteioAdicionarConfirmar('OK?', function () {
                aplicarResultadoIdoso(vencedor.codigo, vagaAtribuida);
            });
        }

        revelarProxima();
    }

    function sorteioAdicionarConfirmar(pergunta, aoConfirmar) {
        var painel = document.getElementById('sorteioPainel');
        var bloco = document.createElement('div');
        bloco.innerHTML = '<p>' + pergunta + '</p>'
                + '<button type="button" class="btn btn-primary sorteio-btn-confirmar">OK</button>';
        painel.appendChild(bloco);
        bloco.querySelector('.sorteio-btn-confirmar').addEventListener('click', function () {
            bloco.querySelector('.sorteio-btn-confirmar').disabled = true;
            aoConfirmar();
        });
    }

    function aplicarResultadoIdoso(codigoUnidade, vagaAtribuida) {
        var celulaPrevia = document.querySelector('[data-unidade-previa="' + codigoUnidade + '"]');
        if (celulaPrevia) {
            celulaPrevia.textContent = vagaAtribuida;
            celulaPrevia.className = 'sorteio-preferencial';
        }
        var celulaPosicao = document.querySelector('[data-unidade-posicao="' + codigoUnidade + '"]');
        if (celulaPosicao) {
            celulaPosicao.className = 'sorteio-ja-sorteada';
        }
        vagaAtribuida.split('|').forEach(function (codigoVaga) {
            var chip = document.querySelector('[data-vaga="' + codigoVaga + '"]');
            if (chip) {
                chip.classList.add('sorteio-vaga-riscada');
            }
        });

        sorteioAdicionarLinha('Aplicado: unidade ' + codigoUnidade + ' → vaga(s) ' + vagaAtribuida + '.');
        setTimeout(function () {
            iniciarSorteioGeral(codigoUnidade);
        }, 900);
    }

    // ---- Fase 2: sorteio das vagas internas/externas para as demais unidades ----
    function iniciarSorteioGeral(codigoExcluido) {
        var painel = document.getElementById('sorteioPainel');
        sorteioAdicionarLinha('Fase 2 — Sorteio das vagas internas/externas').className = 'sorteio-secao-titulo';

        var log = document.createElement('div');
        log.className = 'sorteio-log';
        log.id = 'sorteioLogGeral';
        painel.appendChild(log);

        var celulasPrevia = Array.prototype.slice.call(document.querySelectorAll('[data-unidade-previa]'));
        var unidades = [];
        celulasPrevia.forEach(function (cel) {
            var codigo = cel.getAttribute('data-unidade-previa');
            if (!codigo || codigo === codigoExcluido) {
                return;
            }
            unidades.push({
                codigo: codigo,
                isExterna: cel.classList.contains('sorteio-externa'),
                qtd: sorteioUnidadesVagasGaragem[codigo] || 1,
                sorteadas: []
            });
        });
        embaralhar(unidades);

        // Uma vaga dupla equivale as duas vagas de uma unidade que tem vagasGaragem=2: tenta
        // sortear uma dupla primeiro, e só cai para duas individuais se nao houver dupla sobrando.
        var individuaisInternas = sorteioVagasInternasIndividuais.slice();
        var duplasInternas = sorteioVagasInternasDuplas.slice();
        var individuaisExternas = sorteioVagasExternasIndividuais.slice();
        var duplasExternas = sorteioVagasExternasDuplas.slice();
        embaralhar(individuaisInternas);
        embaralhar(duplasInternas);
        embaralhar(individuaisExternas);
        embaralhar(duplasExternas);

        if (!unidades.length) {
            sorteioAdicionarLinha('Nenhuma unidade pendente para esta fase.');
            finalizarSorteio();
            return;
        }

        var indice = 0;
        var faltantes = [];

        function proximaUnidade() {
            if (indice >= unidades.length) {
                var mensagem = 'Sorteio das vagas internas/externas concluído.';
                if (faltantes.length) {
                    mensagem += ' Sem vaga suficiente para: ' + faltantes.join(', ') + '.';
                }
                sorteioAdicionarConfirmar(mensagem + ' OK?', finalizarSorteio);
                return;
            }
            var u = unidades[indice];
            var poolIndividual = u.isExterna ? individuaisExternas : individuaisInternas;
            var poolDupla = u.isExterna ? duplasExternas : duplasInternas;
            var linha = document.createElement('p');
            linha.className = 'sorteio-texto-live';
            var tipoTexto = u.isExterna ? 'externa' : 'interna';

            if (u.qtd >= 2 && poolDupla.length) {
                var vagaDupla = poolDupla.shift();
                u.sorteadas.push(vagaDupla);
                linha.textContent = 'Unidade ' + u.codigo + ' → vaga ' + vagaDupla + ' (dupla)';
            } else if (u.qtd >= 2) {
                var v1 = poolIndividual.shift();
                if (v1) {
                    u.sorteadas.push(v1);
                }
                var v2 = poolIndividual.shift();
                if (v2) {
                    u.sorteadas.push(v2);
                }
                if (u.sorteadas.length < u.qtd) {
                    faltantes.push(u.codigo);
                    linha.textContent = u.sorteadas.length
                            ? ('Unidade ' + u.codigo + ' → vaga ' + u.sorteadas.join('/') + ' (incompleto, faltou vaga ' + tipoTexto + ')')
                            : ('Vaga ' + tipoTexto + ' insuficiente para a unidade ' + u.codigo + '.');
                } else {
                    linha.textContent = 'Unidade ' + u.codigo + ' → vagas ' + u.sorteadas.join('/');
                }
            } else {
                var vaga = poolIndividual.shift();
                if (!vaga) {
                    faltantes.push(u.codigo);
                    linha.textContent = 'Vaga ' + tipoTexto + ' insuficiente para a unidade ' + u.codigo + '.';
                } else {
                    u.sorteadas.push(vaga);
                    linha.textContent = 'Unidade ' + u.codigo + ' → vaga ' + vaga;
                }
            }

            log.appendChild(linha);
            log.scrollTop = log.scrollHeight;

            indice++;
            setTimeout(proximaUnidade, 700);
        }

        proximaUnidade();

        // Guarda as unidades para aplicar tudo de uma vez quando o usuário confirmar.
        iniciarSorteioGeral.unidadesPendentes = unidades;
    }

    function finalizarSorteio() {
        var unidades = iniciarSorteioGeral.unidadesPendentes || [];
        unidades.forEach(function (u) {
            if (!u.sorteadas.length) {
                return;
            }

            var celulaPrevia = document.querySelector('[data-unidade-previa="' + u.codigo + '"]');
            if (celulaPrevia) {
                celulaPrevia.textContent = u.sorteadas.join('/');
            }
            var celulaPosicao = document.querySelector('[data-unidade-posicao="' + u.codigo + '"]');
            if (celulaPosicao) {
                celulaPosicao.className = 'sorteio-ja-sorteada';
            }
            u.sorteadas.forEach(function (vaga) {
                var chip = document.querySelector('[data-vaga="' + vaga + '"]');
                if (chip) {
                    chip.classList.add('sorteio-vaga-riscada');
                }
            });
        });
        iniciarSorteioGeral.unidadesPendentes = [];

        sorteioAdicionarLinha('Sorteio concluído.');
        sorteioEmAndamento = false;
    }
</script>
</body>
</html>
