(function () {
    'use strict';

    function stripDiacritics(text) {
        var result = '';
        for (var i = 0; i < text.length; i++) {
            var code = text.charCodeAt(i);
            var isCombiningMark = code >= 768 && code <= 879;
            if (!isCombiningMark) {
                result += text.charAt(i);
            }
        }
        return result;
    }

    function normalize(text) {
        return stripDiacritics((text || '').normalize('NFD')).toLowerCase().trim();
    }

    function cellText(cell) {
        return (cell.textContent || '').trim();
    }

    function parseNumber(text) {
        var match = text.replace(/^R\$\s*/, '').match(/-?\d+(\.\d+)?/);
        return match ? Number(match[0]) : null;
    }

    function parseDate(text) {
        var trimmed = text.trim();
        if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) {
            return new Date(trimmed).getTime();
        }
        var brMatch = trimmed.match(/^(\d{2})\/(\d{2})\/(\d{2}|\d{4})(?:\s+(\d{2}):(\d{2})(?::(\d{2}))?)?$/);
        if (brMatch) {
            var day = Number(brMatch[1]);
            var month = Number(brMatch[2]) - 1;
            var year = brMatch[3].length === 2 ? 2000 + Number(brMatch[3]) : Number(brMatch[3]);
            var hour = brMatch[4] ? Number(brMatch[4]) : 0;
            var minute = brMatch[5] ? Number(brMatch[5]) : 0;
            var second = brMatch[6] ? Number(brMatch[6]) : 0;
            return new Date(year, month, day, hour, minute, second).getTime();
        }
        return null;
    }

    function compareValues(a, b) {
        var numA = parseNumber(a);
        var numB = parseNumber(b);
        if (numA !== null && numB !== null) {
            return numA - numB;
        }
        var dateA = parseDate(a);
        var dateB = parseDate(b);
        if (dateA !== null && dateB !== null) {
            return dateA - dateB;
        }
        return normalize(a).localeCompare(normalize(b), 'pt-BR');
    }

    function initDataTable(table) {
        var thead = table.tHead;
        var tbody = table.tBodies[0];
        if (!thead || !tbody) {
            return;
        }
        var headerRow = thead.rows[0];

        var filterRow = document.createElement('tr');
        filterRow.className = 'filter-row';

        Array.prototype.forEach.call(headerRow.cells, function (th, index) {
            var filterCell = document.createElement('th');
            if (!th.hasAttribute('data-no-sort')) {
                var input = document.createElement('input');
                input.type = 'text';
                input.placeholder = 'Filtrar';
                input.setAttribute('aria-label', 'Filtrar esta coluna');
                input.className = 'column-filter';
                input.addEventListener('input', applyFilters);
                filterCell.appendChild(input);

                th.classList.add('sortable-header');
                th.addEventListener('click', function () {
                    sortByColumn(index);
                });
            }
            filterRow.appendChild(filterCell);
        });
        thead.appendChild(filterRow);

        var sortState = { column: -1, ascending: true };

        function getRows() {
            return Array.prototype.slice.call(tbody.rows);
        }

        function applyFilters() {
            var inputs = filterRow.querySelectorAll('.column-filter');
            var filters = Array.prototype.map.call(inputs, function (input) {
                return normalize(input.value);
            });
            getRows().forEach(function (row) {
                var visible = true;
                Array.prototype.forEach.call(row.cells, function (td, index) {
                    if (filters[index] && normalize(cellText(td)).indexOf(filters[index]) === -1) {
                        visible = false;
                    }
                });
                row.style.display = visible ? '' : 'none';
            });
        }

        function sortByColumn(index) {
            var ascending = sortState.column === index ? !sortState.ascending : true;
            sortState = { column: index, ascending: ascending };

            var rows = getRows();
            rows.sort(function (r1, r2) {
                var cmp = compareValues(cellText(r1.cells[index]), cellText(r2.cells[index]));
                return ascending ? cmp : -cmp;
            });
            rows.forEach(function (row) {
                tbody.appendChild(row);
            });

            Array.prototype.forEach.call(headerRow.cells, function (th, i) {
                th.classList.remove('sort-asc', 'sort-desc');
                if (i === index) {
                    th.classList.add(ascending ? 'sort-asc' : 'sort-desc');
                }
            });
        }
    }

    /**
     * Barra de rolagem horizontal fixa no pe da tela (position:fixed;bottom:0), sincronizada com
     * o scroll real da tabela. Sempre visivel enquanto a tabela tiver colunas escondidas, mesmo
     * que a barra "de verdade" (embaixo da tabela) ja esteja visivel na tela - assim o usuario
     * nunca precisa procurar a barra de rolagem em telas diferentes.
     */
    function initStickyScrollbar(tableWrap) {
        var bar = document.createElement('div');
        bar.className = 'sticky-hscroll';
        var inner = document.createElement('div');
        bar.appendChild(inner);
        document.body.appendChild(bar);

        var syncingFromBar = false;
        var syncingFromTable = false;

        function updateGeometry() {
            var rect = tableWrap.getBoundingClientRect();
            var hasOverflow = tableWrap.scrollWidth > tableWrap.clientWidth + 1;

            bar.style.display = hasOverflow ? 'block' : 'none';
            if (!hasOverflow) {
                return;
            }
            bar.style.left = rect.left + 'px';
            bar.style.width = rect.width + 'px';
            inner.style.width = tableWrap.scrollWidth + 'px';
            if (!syncingFromTable) {
                syncingFromBar = true;
                bar.scrollLeft = tableWrap.scrollLeft;
                syncingFromBar = false;
            }
        }

        tableWrap.addEventListener('scroll', function () {
            if (syncingFromBar) {
                return;
            }
            syncingFromTable = true;
            bar.scrollLeft = tableWrap.scrollLeft;
            syncingFromTable = false;
        });

        bar.addEventListener('scroll', function () {
            if (syncingFromTable) {
                return;
            }
            syncingFromBar = true;
            tableWrap.scrollLeft = bar.scrollLeft;
            syncingFromBar = false;
        });

        window.addEventListener('scroll', updateGeometry, { passive: true });
        window.addEventListener('resize', updateGeometry);
        updateGeometry();
    }

    document.addEventListener('DOMContentLoaded', function () {
        var tables = document.querySelectorAll('table.data-table[data-sortable]');
        Array.prototype.forEach.call(tables, initDataTable);

        var tableWraps = document.querySelectorAll('.table-wrap');
        Array.prototype.forEach.call(tableWraps, initStickyScrollbar);
    });
})();
