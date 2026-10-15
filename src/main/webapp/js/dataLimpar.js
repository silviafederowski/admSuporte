/* Acrescenta um botao "Limpar" ao lado de cada campo de data editavel, para deixar a data sem informar. */
(function () {
    'use strict';

    function adicionarBotaoLimpar(input) {
        if (input.disabled || input.readOnly) {
            return;
        }
        var wrapper = document.createElement('div');
        wrapper.className = 'date-clear';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        var botao = document.createElement('button');
        botao.type = 'button';
        botao.className = 'btn btn-secondary btn-small';
        botao.textContent = 'Limpar';
        botao.title = 'Deixar sem data';
        botao.addEventListener('click', function () {
            input.value = '';
            input.dispatchEvent(new Event('change', { bubbles: true }));
            input.focus();
        });
        wrapper.appendChild(botao);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var campos = document.querySelectorAll('input[type="date"]');
        for (var i = 0; i < campos.length; i++) {
            adicionarBotaoLimpar(campos[i]);
        }
    });
})();
