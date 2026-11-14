/* Acrescenta um icone de olho dentro de cada campo de senha editavel, para mostrar/ocultar o que foi digitado. */
(function () {
    'use strict';

    // Olho aberto: mostrar a senha. Olho fechado (riscado): ocultar de novo. SVG, sem depender de emoji/fonte.
    var OLHO_ABERTO =
        '<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none" stroke="currentColor" ' +
        'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
        '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
    var OLHO_FECHADO =
        '<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none" stroke="currentColor" ' +
        'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
        '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>' +
        '<path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>' +
        '<path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';

    function adicionarBotaoOlho(input) {
        if (input.disabled || input.readOnly) {
            return;
        }
        var wrapper = document.createElement('div');
        wrapper.className = 'senha-wrap';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        var botao = document.createElement('button');
        botao.type = 'button';
        botao.className = 'senha-toggle';
        function atualizar(visivel) {
            input.type = visivel ? 'text' : 'password';
            botao.innerHTML = visivel ? OLHO_FECHADO : OLHO_ABERTO;
            botao.title = visivel ? 'Ocultar senha' : 'Mostrar senha';
            botao.setAttribute('aria-label', botao.title);
            botao.setAttribute('aria-pressed', visivel ? 'true' : 'false');
        }

        botao.addEventListener('click', function () {
            atualizar(input.type === 'password');
            input.focus();
        });
        atualizar(false);
        wrapper.appendChild(botao);

        // Garante que a senha nunca seja enviada/salva pelo navegador como campo de texto comum.
        if (input.form) {
            input.form.addEventListener('submit', function () {
                atualizar(false);
            });
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        var campos = document.querySelectorAll('input[type="password"]');
        for (var i = 0; i < campos.length; i++) {
            adicionarBotaoOlho(campos[i]);
        }
    });
})();
