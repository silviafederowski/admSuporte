<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="readOnly" value="${sessionScope.userNivel != 0}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Autorizações - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=5">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Autorizações</h1>
            </div>
            <div class="toolbar-actions">
                <c:if test="${!readOnly}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/autorizacoes/form">Nova autorização</a>
                </c:if>
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>
        <p class="subtitle">
            Define quem enxerga cada opção do menu e se pode só consultar ou também editar.
            Sem uma linha para o nível, a opção nem aparece no menu.
            <c:if test="${readOnly}"> Apenas o síndico pode editar.</c:if>
        </p>

        <c:if test="${not empty infoMessage}">
            <p class="form-info">${infoMessage}</p>
        </c:if>
        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty autorizacoes}">
                <p class="empty-state">Nenhuma autorização cadastrada ainda.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Tela</th>
                            <th>Descrição</th>
                            <th>Nível</th>
                            <th>Acesso</th>
                            <c:if test="${!readOnly}"><th>Excluir</th></c:if>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="a" items="${autorizacoes}">
                            <tr class="clickable-row"
                                onclick="window.location='${pageContext.request.contextPath}/autorizacoes/form?id=${a.id}'">
                                <td>${a.telaCodigo}</td>
                                <td>${a.telaDescricao}</td>
                                <td class="nivel-cell" title="Ver tipos deste nível"
                                    onclick="event.stopPropagation(); showTiposNivel(event, ${a.nivel});">${a.nivelFormatado}</td>
                                <td>${a.tipoAcesso == 'edicao' ? 'Edição' : 'Consulta'}</td>
                                <c:if test="${!readOnly}">
                                    <td onclick="event.stopPropagation();">
                                        <form method="post" action="${pageContext.request.contextPath}/autorizacoes/excluir"
                                              onsubmit="return confirm('Excluir esta autorização?');" style="display:inline;">
                                            <input type="hidden" name="id" value="${a.id}">
                                            <button type="submit" class="icon-link" title="Excluir autorização" aria-label="Excluir autorização"
                                                    style="border:none;background:none;padding:0;font:inherit;color:inherit;">🗑️</button>
                                        </form>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/menu">Voltar ao menu</a>
    </div>
</div>

<div id="nivelPopup" class="info-popup" hidden>
    <button type="button" class="info-popup-close" aria-label="Fechar">&times;</button>
    <div class="info-popup-title">Tipos com este nível</div>
    <ul id="nivelPopupList"></ul>
</div>

<script src="${pageContext.request.contextPath}/js/dataTable.js?v=5"></script>
<script>
    var TIPOS_POR_NIVEL = {};
    <c:forEach var="ut" items="${userTypes}">
    (TIPOS_POR_NIVEL[${ut.nivel}] = TIPOS_POR_NIVEL[${ut.nivel}] || []).push("${ut.name}");
    </c:forEach>

    (function () {
        var popup = document.getElementById('nivelPopup');
        var list = document.getElementById('nivelPopupList');
        var closeBtn = popup.querySelector('.info-popup-close');

        window.showTiposNivel = function (event, nivel) {
            var tipos = TIPOS_POR_NIVEL[nivel] || [];
            list.innerHTML = '';
            if (tipos.length === 0) {
                var li = document.createElement('li');
                li.textContent = 'Nenhum tipo de usuário com este nível.';
                list.appendChild(li);
            } else {
                tipos.forEach(function (nome) {
                    var li = document.createElement('li');
                    li.textContent = nome;
                    list.appendChild(li);
                });
            }

            popup.hidden = false;
            var rect = event.target.getBoundingClientRect();
            var popupWidth = popup.offsetWidth || 220;
            var left = Math.min(rect.left, window.innerWidth - popupWidth - 16);
            left = Math.max(left, 8);
            var top = rect.bottom + 8;
            if (top + popup.offsetHeight > window.innerHeight) {
                top = Math.max(rect.top - 8 - popup.offsetHeight, 8);
            }
            popup.style.left = left + 'px';
            popup.style.top = top + 'px';
        };

        function closePopup() {
            popup.hidden = true;
        }

        closeBtn.addEventListener('click', function (event) {
            event.stopPropagation();
            closePopup();
        });

        document.addEventListener('click', function (event) {
            if (!popup.hidden && !popup.contains(event.target) && !event.target.closest('.nivel-cell')) {
                closePopup();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closePopup();
            }
        });
    })();
</script>
</body>
</html>
