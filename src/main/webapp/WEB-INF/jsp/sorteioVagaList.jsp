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
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-externa"></span> Externa</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch"></span> Dupla (sem borda)</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-borda-individual"></span> Individual (borda preta)</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-especial"></span> Especial</span>
                <span class="sorteio-legenda-item"><span class="sorteio-swatch sorteio-preferencial"></span> Idoso</span>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="menu-icon-link" href="javascript:void(0)" onclick="iniciarSorteioIdoso()" title="Sortear vaga de idoso" aria-label="Sortear vaga de idoso">🎲</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <form id="sorteioPdfForm" method="post" action="${pageContext.request.contextPath}/sorteio-vagas/pdf" style="display:none;">
            <textarea name="linhas"></textarea>
        </form>

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
    var sorteioEstado = null;

    // Trava a altura da 3a coluna (Sorteio) na medida inicial, para que só ela role
    // verticalmente conforme o conteúdo cresce, em vez de esticar a linha inteira da grade.
    (function travarAlturaSorteioPainel() {
        var painel = document.getElementById('sorteioPainel');
        if (painel) {
            painel.style.maxHeight = painel.clientHeight + 'px';
        }
    })();

    // Junta o texto de todas as linhas ja exibidas na coluna "Sorteio" (titulos de fase e
    // resultados por unidade), na ordem em que apareceram, sem o texto dos botoes "OK".
    function sorteioColetarLinhas() {
        var painel = document.getElementById('sorteioPainel');
        var elementos = painel.querySelectorAll('.sorteio-texto-live, .sorteio-secao-titulo');
        var linhas = [];
        elementos.forEach(function (el) {
            var clone = el.cloneNode(true);
            var botoes = clone.querySelectorAll('button');
            botoes.forEach(function (b) { b.parentNode.removeChild(b); });
            var texto = clone.textContent.replace(/\s+/g, ' ').trim();
            if (texto) {
                linhas.push(texto);
            }
        });
        return linhas;
    }

    // Chamado automaticamente ao final do sorteio (finalizarSorteio), sem exigir clique do
    // usuário: baixa o PDF com a transcrição completa da coluna "Sorteio".
    function gerarPdfSorteio() {
        var linhas = sorteioColetarLinhas();
        if (!linhas.length) {
            return;
        }
        document.querySelector('#sorteioPdfForm textarea[name="linhas"]').value = linhas.join('\n');
        document.getElementById('sorteioPdfForm').submit();
    }

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

    function sorteioRiscarVaga(codigoVaga) {
        var chip = document.querySelector('[data-vaga="' + codigoVaga + '"]');
        if (chip) {
            chip.classList.add('sorteio-vaga-riscada');
        }
    }

    // Marca a unidade na Prévia com borda vermelha quando o sorteio não conseguiu vaga para ela.
    function sorteioMarcarPreviaInsuficiente(codigoUnidade) {
        var celulaPrevia = document.querySelector('[data-unidade-previa="' + codigoUnidade + '"]');
        if (celulaPrevia) {
            celulaPrevia.classList.add('sorteio-previa-insuficiente');
        }
    }

    // Aplica o resultado (grade de prévia, grade de posição e tachado das vagas) no momento em
    // que a unidade é sorteada, e não apenas depois do OK final da fase. Recebe um ARRAY com os
    // codigos das vagas sorteadas (cada um ja e o codigo completo do chip, mesmo que contenha
    // "|" — vaga dupla propria ja usa "|" no proprio codigo, ex. "021|022" — por isso nunca se
    // reconstroi essa lista fazendo split('|') de volta, só join('|') para exibição).
    function sorteioAplicarResultadoVisual(codigoUnidade, vagasArray, cssPreviaClasse) {
        var textoExibicao = vagasArray.join('|');
        var celulaPrevia = document.querySelector('[data-unidade-previa="' + codigoUnidade + '"]');
        if (celulaPrevia) {
            celulaPrevia.textContent = textoExibicao;
            if (cssPreviaClasse) {
                celulaPrevia.className = cssPreviaClasse;
            }
            celulaPrevia.classList.add('sorteio-previa-atribuida');
            celulaPrevia.classList.remove('sorteio-previa-insuficiente');
        }
        var celulaPosicao = document.querySelector('[data-unidade-posicao="' + codigoUnidade + '"]');
        if (celulaPosicao) {
            celulaPosicao.className = 'sorteio-ja-sorteada';
        }
        vagasArray.forEach(sorteioRiscarVaga);
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
            sorteioAdicionarLinhaComBotao('Nenhuma unidade candidata a vaga de idoso.', function () {
                iniciarSorteioGeral(null);
            });
            return;
        }

        var textoEl = sorteioAdicionarLinha('Candidatos: ');
        var codigosRevelados = [];
        var indice = 0;

        function revelarProxima() {
            codigosRevelados.push(sorteioCandidatos[indice].codigo);
            var ultimaDaVez = indice === sorteioCandidatos.length - 1;
            textoEl.textContent = 'Candidatos: '
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
            var vagasArray = (vencedor.vagasGaragem >= 2 && sorteioVagasIdoso.length >= 2)
                    ? [sorteioVagasIdoso[0], sorteioVagasIdoso[1]]
                    : [sorteioVagasIdoso[0]];

            sorteioAplicarResultadoVisual(vencedor.codigo, vagasArray, 'sorteio-preferencial');
            sorteioAdicionarLinhaComBotao('Contemplada: ' + vencedor.codigo + ' — vaga(s): ' + vagasArray.join('|'), function () {
                iniciarSorteioGeral(vencedor.codigo);
            });
        }

        revelarProxima();
    }

    // Adiciona uma linha com o botão de confirmação ao final do próprio texto (sem pergunta
    // "OK?" nem bloco separado); se não couber, o botão quebra para a linha seguinte sozinho.
    function sorteioMontarLinhaComBotao(linha, texto, aoConfirmar) {
        linha.classList.add('sorteio-linha-inline');
        linha.textContent = '';
        var span = document.createElement('span');
        span.textContent = texto;
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn btn-primary btn-small sorteio-btn-confirmar';
        btn.textContent = 'OK';
        linha.appendChild(span);
        linha.appendChild(btn);
        btn.addEventListener('click', function () {
            btn.disabled = true;
            aoConfirmar();
        });
    }

    function sorteioAdicionarLinhaComBotao(texto, aoConfirmar, container) {
        var alvo = container || document.getElementById('sorteioPainel');
        var linha = document.createElement('p');
        linha.className = 'sorteio-texto-live';
        alvo.appendChild(linha);
        sorteioMontarLinhaComBotao(linha, texto, aoConfirmar);
        alvo.scrollTop = alvo.scrollHeight;
        return linha;
    }

    // Roda uma fase: sorteia, uma a uma, as unidades candidatas, aplicando o resultado
    // (tachado + grade) imediatamente a cada sorteio. Ao terminar, em vez de uma linha de
    // resumo separada, o botão de confirmação é anexado ao final da própria última linha.
    function sorteioExecutarFase(tituloFase, unidadesCandidatas, sortear, aoConcluir) {
        var painel = document.getElementById('sorteioPainel');
        sorteioAdicionarLinha(tituloFase).className = 'sorteio-secao-titulo';

        if (!unidadesCandidatas.length) {
            sorteioAdicionarLinhaComBotao('Nenhuma unidade pendente para esta fase.', aoConcluir);
            return;
        }

        var log = document.createElement('div');
        log.className = 'sorteio-log';
        painel.appendChild(log);

        // A partir da fase 2, as unidades são percorridas em ordem crescente de código — só a
        // vaga sorteada para cada uma é que vem de um valor aleatório (pool já embaralhada).
        unidadesCandidatas.sort(function (a, b) {
            return parseInt(a.codigo, 10) - parseInt(b.codigo, 10);
        });

        var indice = 0;

        function proxima() {
            var u = unidadesCandidatas[indice];
            var linha = document.createElement('p');
            linha.className = 'sorteio-texto-live';

            // Nunca deve travar o sorteio no meio: um erro inesperado numa unidade fica
            // registrado na própria linha e no console, e o processamento segue para a próxima.
            try {
                var resultado = sortear(u);
                linha.textContent = resultado.texto;
                if (resultado.ok) {
                    u.atendida = true;
                    sorteioAplicarResultadoVisual(u.codigo, u.sorteadas, null);
                } else {
                    sorteioMarcarPreviaInsuficiente(u.codigo);
                }
            } catch (erro) {
                linha.textContent = 'Erro ao processar a unidade ' + u.codigo + ' — pulando para a próxima.';
                sorteioMarcarPreviaInsuficiente(u.codigo);
                if (window.console && console.error) {
                    console.error('Sorteio: erro ao processar unidade', u.codigo, erro);
                }
            }

            log.appendChild(linha);
            log.scrollTop = log.scrollHeight;

            indice++;
            if (indice < unidadesCandidatas.length) {
                setTimeout(proxima, 700);
            } else {
                sorteioMontarLinhaComBotao(linha, linha.textContent, aoConcluir);
                log.scrollTop = log.scrollHeight;
            }
        }

        proxima();
    }

    function iniciarSorteioGeral(codigoExcluido) {
        var celulasPrevia = Array.prototype.slice.call(document.querySelectorAll('[data-unidade-previa]'));
        var unidades = [];
        celulasPrevia.forEach(function (cel) {
            var codigo = cel.getAttribute('data-unidade-previa');
            if (!codigo || codigo === codigoExcluido) {
                return;
            }
            var celulaPosicao = document.querySelector('[data-unidade-posicao="' + codigo + '"]');
            unidades.push({
                codigo: codigo,
                isExterna: cel.classList.contains('sorteio-externa'),
                hojeInterna: !!(celulaPosicao && celulaPosicao.classList.contains('sorteio-interna')),
                qtd: sorteioUnidadesVagasGaragem[codigo] || 1,
                sorteadas: [],
                atendida: false
            });
        });

        var individuaisInternas = sorteioVagasInternasIndividuais.slice();
        var duplasInternas = sorteioVagasInternasDuplas.slice();
        var individuaisExternas = sorteioVagasExternasIndividuais.slice();
        var duplasExternas = sorteioVagasExternasDuplas.slice();
        embaralhar(individuaisInternas);
        embaralhar(duplasInternas);
        embaralhar(individuaisExternas);
        embaralhar(duplasExternas);

        sorteioEstado = {
            unidades: unidades,
            individuaisInternas: individuaisInternas,
            duplasInternas: duplasInternas,
            individuaisExternas: individuaisExternas,
            duplasExternas: duplasExternas,
            fase7Unidades: []
        };

        iniciarFase2Externas();
    }

    // ---- Fase 2: vagas externas para unidades com uma vaga ----
    function iniciarFase2Externas() {
        var candidatas = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd < 2 && u.isExterna;
        });
        sorteioExecutarFase('Fase 2 — Vagas externas (unidades com uma vaga)', candidatas,
            function (u) {
                var vaga = sorteioEstado.individuaisExternas.shift();
                if (!vaga) {
                    return { texto: 'Vaga externa insuficiente para a unidade ' + u.codigo + '.', ok: false };
                }
                u.sorteadas.push(vaga);
                return { texto: 'Unidade ' + u.codigo + ' → vaga ' + vaga, ok: true };
            },
            iniciarFase3Internas);
    }

    // ---- Fase 3: vagas internas para unidades com uma vaga ----
    function iniciarFase3Internas() {
        var candidatas = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd < 2 && !u.isExterna;
        });
        sorteioExecutarFase('Fase 3 — Vagas internas (unidades com uma vaga)', candidatas,
            function (u) {
                var vaga = sorteioEstado.individuaisInternas.shift();
                if (!vaga) {
                    return { texto: 'Vaga interna insuficiente para a unidade ' + u.codigo + '.', ok: false };
                }
                u.sorteadas.push(vaga);
                return { texto: 'Unidade ' + u.codigo + ' → vaga ' + vaga, ok: true };
            },
            iniciarFase4DuplasHojeInternaParaExterna);
    }

    // Se não houver vaga dupla externa para uma unidade hoje interna que deveria ir para
    // externa, ela passa a ser tratada como interna (atualiza a cor na Prévia) e continua
    // concorrendo nas próximas fases, em vez de ficar simplesmente sem vaga.
    function sorteioReclassificarComoInterna(u) {
        u.isExterna = false;
        var celulaPrevia = document.querySelector('[data-unidade-previa="' + u.codigo + '"]');
        if (celulaPrevia) {
            celulaPrevia.classList.remove('sorteio-externa');
            celulaPrevia.classList.add('sorteio-interna');
        }
    }

    // ---- Fase 4: unidades hoje internas que devem ir para externas (duplas) recebem vaga dupla externa ----
    function iniciarFase4DuplasHojeInternaParaExterna() {
        var candidatas = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd >= 2 && u.hojeInterna && u.isExterna;
        });
        sorteioExecutarFase('Fase 4 — Vagas duplas (interna → externa)', candidatas,
            function (u) {
                var vaga = sorteioEstado.duplasExternas.shift();
                if (!vaga) {
                    sorteioReclassificarComoInterna(u);
                    return {
                        texto: 'Vaga dupla externa insuficiente para a unidade ' + u.codigo
                                + ' — passa a concorrer como interna nas próximas fases.',
                        ok: false
                    };
                }
                u.sorteadas.push(vaga);
                return { texto: 'Unidade ' + u.codigo + ' → vaga ' + vaga, ok: true };
            },
            iniciarFase5DuplasHojeExternaParaInterna);
    }

    // ---- Fase 5: unidades hoje externas que devem ir para internas (duplas) recebem vaga dupla interna ----
    function iniciarFase5DuplasHojeExternaParaInterna() {
        var candidatas = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd >= 2 && !u.hojeInterna && !u.isExterna;
        });
        sorteioExecutarFase('Fase 5 — Vagas duplas (externa → interna)', candidatas,
            function (u) {
                var vaga = sorteioEstado.duplasInternas.shift();
                if (!vaga) {
                    return { texto: 'Vaga dupla interna insuficiente para a unidade ' + u.codigo + '.', ok: false };
                }
                u.sorteadas.push(vaga);
                return { texto: 'Unidade ' + u.codigo + ' → vaga ' + vaga, ok: true };
            },
            iniciarFase6DuplasHojeInternaPreviaInterna);
    }

    // ---- Fase 6: unidades hoje internas que continuam internas (duplas) recebem vaga dupla interna ----
    function iniciarFase6DuplasHojeInternaPreviaInterna() {
        var candidatas = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd >= 2 && u.hojeInterna && !u.isExterna;
        });
        sorteioExecutarFase('Fase 6 — Vagas duplas (interna → interna)', candidatas,
            function (u) {
                var vaga = sorteioEstado.duplasInternas.shift();
                if (!vaga) {
                    return { texto: 'Vaga dupla interna insuficiente para a unidade ' + u.codigo + '.', ok: false };
                }
                u.sorteadas.push(vaga);
                return { texto: 'Unidade ' + u.codigo + ' → vaga ' + vaga, ok: true };
            },
            iniciarFase7Empate);
    }

    function sorteioRemoverDoPool(pool, codigo) {
        var idx = pool.indexOf(codigo);
        if (idx === -1) {
            return false;
        }
        pool.splice(idx, 1);
        return true;
    }

    // ---- Fase 7: se sobrarem duas vagas simples e faltar uma vaga dupla, une os códigos com | ----
    function iniciarFase7Empate() {
        var pendentes = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida && u.qtd >= 2;
        });

        if (!pendentes.length) {
            finalizarSorteio();
            return;
        }

        sorteioExecutarFase('Fase 7 — Ajuste final (união de vagas simples restantes)', pendentes,
            function (u) {
                var sobrasSimples = sorteioEstado.individuaisInternas.concat(sorteioEstado.individuaisExternas);
                if (sobrasSimples.length < 2) {
                    return { texto: 'Sem vagas simples suficientes para unir na unidade ' + u.codigo + '.', ok: false };
                }
                var v1 = sobrasSimples[0];
                var v2 = sobrasSimples[1];
                sorteioRemoverDoPool(sorteioEstado.individuaisInternas, v1)
                        || sorteioRemoverDoPool(sorteioEstado.individuaisExternas, v1);
                sorteioRemoverDoPool(sorteioEstado.individuaisInternas, v2)
                        || sorteioRemoverDoPool(sorteioEstado.individuaisExternas, v2);
                u.sorteadas.push(v1, v2);
                sorteioEstado.fase7Unidades.push(u.codigo);
                return { texto: 'Unidade ' + u.codigo + ' → vagas ' + v1 + '|' + v2 + ' (união de vagas simples)', ok: true };
            },
            finalizarSorteio);
    }

    function finalizarSorteio() {
        var faltantes = sorteioEstado.unidades.filter(function (u) {
            return !u.atendida;
        }).map(function (u) {
            return u.codigo;
        });
        if (faltantes.length) {
            sorteioAdicionarLinha('Atenção: unidades sem vaga sorteada ao final: ' + faltantes.join(', ') + '.');
        }
        if (sorteioEstado.fase7Unidades.length) {
            sorteioAdicionarLinha('Atenção: foi necessário aplicar a Fase 7 (união de duas vagas simples com "|") para: '
                    + sorteioEstado.fase7Unidades.join(', ') + '.');
        }

        sorteioAdicionarLinha('Sorteio concluído.');
        sorteioEstado = null;
        sorteioEmAndamento = false;
        gerarPdfSorteio();
    }
</script>
</body>
</html>
