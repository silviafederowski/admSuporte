<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vagas - admSuporte</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="page">
    <div class="card card-wide">
        <div class="toolbar">
            <div>
                <h1 class="title" style="text-align:left;margin:0;">Vagas</h1>
            </div>
            <div class="toolbar-actions">
                <a class="menu-icon-link" href="${pageContext.request.contextPath}/menu" title="Voltar ao menu" aria-label="Voltar ao menu">🏠</a>
            </div>
        </div>

        <c:if test="${not empty formError}">
            <p class="form-error">${formError}</p>
        </c:if>

        <c:choose>
            <c:when test="${empty historico}">
                <p class="empty-state">Nenhum registro encontrado.</p>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table" data-sortable>
                        <thead>
                        <tr>
                            <th>Ano</th>
                            <th>Unidade</th>
                            <th>Vaga</th>
                            <th>Pavimento</th>
                            <th>Int/Ext</th>
                            <th>Especial</th>
                            <th>Observação</th>
                            <th data-no-sort>Veículos</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="h" items="${historico}">
                            <tr data-pavimento="${h.vagaPavimento}">
                                <td>${h.ano}</td>
                                <td>${h.unidadeCodigo}</td>
                                <td>${h.vagaCodigo}</td>
                                <td class="pavimento-cell" title="Ver imagem do pavimento">${h.vagaPavimento}</td>
                                <td>${h.vagaInternaExterna}</td>
                                <td>${h.vagaEspecial}</td>
                                <td>${h.observacaoHistorico}</td>
                                <td>
                                    <c:if test="${not empty h.unidadeChave}">
                                        <a class="icon-link" title="Veículos desta unidade" aria-label="Veículos desta unidade"
                                           href="${pageContext.request.contextPath}/veiculos/form?unidade=${h.unidadeChave}"
                                           onclick="event.stopPropagation();">🚗</a>
                                    </c:if>
                                </td>
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

<div id="pavimentoPopup" class="image-popup" hidden>
    <div class="image-popup-toolbar">
        <div class="image-popup-drag-handle" title="Arraste para mover">Arraste para mover</div>
        <button type="button" class="image-popup-zoom" data-zoom-action="out" aria-label="Diminuir zoom">&minus;</button>
        <button type="button" class="image-popup-zoom" data-zoom-action="reset" aria-label="Redefinir zoom">100%</button>
        <button type="button" class="image-popup-zoom" data-zoom-action="in" aria-label="Aumentar zoom">+</button>
        <button type="button" class="image-popup-close" aria-label="Fechar">&times;</button>
    </div>
    <div id="pavimentoPopupViewport" class="image-popup-viewport">
        <img id="pavimentoPopupImg" src="" alt="Imagem do pavimento">
    </div>
    <div id="pavimentoPopupResize" class="image-popup-resize-handle" title="Arraste para redimensionar"></div>
</div>

<script src="${pageContext.request.contextPath}/js/dataTable.js"></script>
<script>
    (function () {
        var contextPath = '${pageContext.request.contextPath}';
        var popup = document.getElementById('pavimentoPopup');
        var viewport = document.getElementById('pavimentoPopupViewport');
        var popupImg = document.getElementById('pavimentoPopupImg');
        var closeBtn = popup.querySelector('.image-popup-close');
        var zoomButtons = popup.querySelectorAll('.image-popup-zoom');
        var dragHandle = popup.querySelector('.image-popup-drag-handle');
        var resizeHandle = document.getElementById('pavimentoPopupResize');

        var MIN_ZOOM = 1;
        var MAX_ZOOM = 4;
        var zoom = 1;
        var panX = 0;
        var panY = 0;
        var dragging = false;
        var dragMoved = false;
        var dragStart = { x: 0, y: 0 };
        var panStart = { x: 0, y: 0 };

        var MIN_POPUP_WIDTH = 260;
        var MIN_POPUP_HEIGHT = 220;
        var movingPopup = false;
        var moveStart = { x: 0, y: 0 };
        var popupStart = { left: 0, top: 0 };
        var resizingPopup = false;
        var resizeStart = { x: 0, y: 0, width: 0, height: 0 };

        function applyTransform() {
            popupImg.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + zoom + ')';
            popupImg.classList.toggle('is-zoomed', zoom > 1);
        }

        function setZoom(newZoom) {
            zoom = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, newZoom));
            if (zoom === MIN_ZOOM) {
                panX = 0;
                panY = 0;
            }
            applyTransform();
        }

        function resetZoom() {
            panX = 0;
            panY = 0;
            setZoom(MIN_ZOOM);
        }

        function closePopup() {
            popup.hidden = true;
            popupImg.src = '';
            resetZoom();
        }

        function positionPopup(row) {
            var rowRect = row.getBoundingClientRect();
            var popupWidth = popup.offsetWidth || 320;
            var popupHeight = popup.offsetHeight || 200;
            var left = Math.min(rowRect.left, window.innerWidth - popupWidth - 16);
            left = Math.max(left, 8);
            var top = rowRect.bottom + 8;
            if (top + popupHeight > window.innerHeight) {
                top = Math.max(rowRect.top - 8 - popupHeight, 8);
            }
            popup.style.left = left + 'px';
            popup.style.top = top + 'px';
        }

        function selectRow(row) {
            document.querySelectorAll('tbody tr.selected-row').forEach(function (other) {
                if (other !== row) {
                    other.classList.remove('selected-row');
                }
            });
            row.classList.add('selected-row');
        }

        function openPopupForRow(row) {
            selectRow(row);
            var pavimento = row.getAttribute('data-pavimento');
            if (!pavimento) {
                return;
            }
            resetZoom();
            popupImg.onload = function () {
                positionPopup(row);
            };
            popupImg.src = contextPath + '/imagem-pavimento?pavimento=' + encodeURIComponent(pavimento);
            popup.hidden = false;
            positionPopup(row);
        }

        document.querySelectorAll('td.pavimento-cell').forEach(function (cell) {
            cell.addEventListener('click', function (event) {
                event.stopPropagation();
                openPopupForRow(cell.closest('tr'));
            });
        });

        closeBtn.addEventListener('click', function (event) {
            event.stopPropagation();
            closePopup();
        });

        zoomButtons.forEach(function (btn) {
            btn.addEventListener('click', function (event) {
                event.stopPropagation();
                var action = btn.getAttribute('data-zoom-action');
                if (action === 'in') {
                    setZoom(zoom + 0.5);
                } else if (action === 'out') {
                    setZoom(zoom - 0.5);
                } else {
                    resetZoom();
                }
            });
        });

        popupImg.addEventListener('click', function (event) {
            event.stopPropagation();
            if (dragMoved) {
                return;
            }
            setZoom(zoom > 1 ? 1 : 2.5);
        });

        viewport.addEventListener('wheel', function (event) {
            event.preventDefault();
            setZoom(zoom + (event.deltaY < 0 ? 0.25 : -0.25));
        }, { passive: false });

        viewport.addEventListener('mousedown', function (event) {
            if (zoom <= 1) {
                return;
            }
            dragging = true;
            dragMoved = false;
            dragStart = { x: event.clientX, y: event.clientY };
            panStart = { x: panX, y: panY };
            event.preventDefault();
        });

        dragHandle.addEventListener('mousedown', function (event) {
            movingPopup = true;
            moveStart = { x: event.clientX, y: event.clientY };
            var rect = popup.getBoundingClientRect();
            popupStart = { left: rect.left, top: rect.top };
            event.preventDefault();
        });

        resizeHandle.addEventListener('mousedown', function (event) {
            resizingPopup = true;
            resizeStart = {
                x: event.clientX,
                y: event.clientY,
                width: popup.offsetWidth,
                height: popup.offsetHeight
            };
            event.preventDefault();
            event.stopPropagation();
        });

        document.addEventListener('mousemove', function (event) {
            if (movingPopup) {
                var newLeft = popupStart.left + (event.clientX - moveStart.x);
                var newTop = popupStart.top + (event.clientY - moveStart.y);
                newLeft = Math.min(Math.max(newLeft, 0), window.innerWidth - popup.offsetWidth);
                newTop = Math.min(Math.max(newTop, 0), window.innerHeight - popup.offsetHeight);
                popup.style.left = newLeft + 'px';
                popup.style.top = newTop + 'px';
                return;
            }
            if (resizingPopup) {
                var maxWidth = window.innerWidth - popup.getBoundingClientRect().left - 8;
                var maxHeight = window.innerHeight - popup.getBoundingClientRect().top - 8;
                var newWidth = Math.min(Math.max(resizeStart.width + (event.clientX - resizeStart.x), MIN_POPUP_WIDTH), maxWidth);
                var newHeight = Math.min(Math.max(resizeStart.height + (event.clientY - resizeStart.y), MIN_POPUP_HEIGHT), maxHeight);
                popup.style.width = newWidth + 'px';
                popup.style.height = newHeight + 'px';
                return;
            }
            if (!dragging) {
                return;
            }
            var dx = event.clientX - dragStart.x;
            var dy = event.clientY - dragStart.y;
            if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
                dragMoved = true;
            }
            panX = panStart.x + dx;
            panY = panStart.y + dy;
            applyTransform();
        });

        document.addEventListener('mouseup', function () {
            dragging = false;
            movingPopup = false;
            resizingPopup = false;
        });

        document.addEventListener('click', function (event) {
            if (!popup.hidden && !popup.contains(event.target) && !event.target.closest('td.pavimento-cell')) {
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
