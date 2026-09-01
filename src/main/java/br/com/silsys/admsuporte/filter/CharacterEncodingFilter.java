package br.com.silsys.admsuporte.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Forca UTF-8 na leitura dos parametros da requisicao (POST/GET) e na resposta.
 *
 * Sem isso, o container decodifica o corpo dos formularios como ISO-8859-1 por padrao
 * (comportamento historico do Servlet API), o que corrompe qualquer caractere acentuado
 * digitado pelo usuario (ex.: "ç" vira dois caracteres invalidos). Precisa rodar antes de
 * qualquer chamada a request.getParameter(...), entao fica mapeado em /* como primeiro filtro.
 */
public class CharacterEncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) {
        // sem configuracao necessaria
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(ENCODING);
        response.setCharacterEncoding(ENCODING);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // sem recursos a liberar
    }
}
