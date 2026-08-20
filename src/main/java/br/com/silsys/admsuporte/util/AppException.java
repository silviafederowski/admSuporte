package br.com.silsys.admsuporte.util;

/**
 * Erro de negocio com mensagem segura para exibir diretamente ao usuario
 * (ex.: "ja existe uma conta com este e-mail"), equivalente ao AppError do app mobile.
 */
public class AppException extends Exception {

    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }
}
