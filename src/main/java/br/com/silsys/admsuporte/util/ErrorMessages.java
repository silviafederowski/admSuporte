package br.com.silsys.admsuporte.util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traduz SQLException para uma mensagem amigavel (em vez do texto tecnico do driver JDBC) para
 * mostrar ao usuario, sempre registrando o erro original no log da aplicacao junto com o codigo
 * da tela e a operacao em que ocorreu, para diagnostico.
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    /** So registra o erro original no log (codigo da tela + operacao), sem mensagem para o usuario. */
    public static void logErro(Logger logger, String telaCodigo, String operacao, Throwable erro) {
        logger.log(Level.SEVERE, "[" + telaCodigo + "] " + operacao, erro);
    }

    /** Registra o erro original no log (codigo da tela + operacao) e devolve uma mensagem amigavel para o usuario. */
    public static String friendly(Logger logger, String telaCodigo, String operacao, SQLException erro) {
        logErro(logger, telaCodigo, operacao, erro);
        return friendly(erro);
    }

    /**
     * Mesma ideia, para erros que nao sao SQLException (falha de integracao externa, upload
     * invalido etc.): sempre registra o erro original no log e devolve uma mensagem generica,
     * ja que so SQLException tem codigo/estado padronizado para traduzir com precisao.
     */
    public static String friendly(Logger logger, String telaCodigo, String operacao, Throwable erro) {
        logErro(logger, telaCodigo, operacao, erro);
        if (erro instanceof SQLException) {
            return friendly((SQLException) erro);
        }
        return "Ocorreu um erro inesperado. Tente novamente em instantes; se o problema persistir, contate o suporte.";
    }

    /**
     * Mensagem amigavel para mostrar ao usuario no lugar do texto tecnico do driver JDBC.
     * Reconhece os erros mais comuns do MySQL (chave duplicada, violacao de chave estrangeira,
     * campo obrigatorio ausente, valor grande/invalido demais, falha de conexao); em qualquer
     * outro caso, cai numa mensagem generica - o erro original sempre fica no log (ver logErro).
     */
    public static String friendly(SQLException erro) {
        for (SQLException atual = erro; atual != null; atual = atual.getNextException()) {
            String traduzida = traduzirUma(atual);
            if (traduzida != null) {
                return traduzida;
            }
        }
        return "Ocorreu um erro ao acessar o banco de dados. Tente novamente em instantes; se o problema persistir, contate o suporte.";
    }

    private static String traduzirUma(SQLException erro) {
        switch (erro.getErrorCode()) {
            case 1062:
                return "Já existe um registro com esses dados.";
            case 1451:
                return "Não é possível excluir: existem outros registros vinculados a este.";
            case 1452:
                return "O registro relacionado (serviço, prestador, tipo de usuário etc.) não existe ou já foi excluído. Atualize a página e tente novamente.";
            case 1048:
                return "Um campo obrigatório não foi preenchido.";
            case 1406:
                return "Um dos campos preenchidos ultrapassa o tamanho permitido.";
            case 1264:
                return "Um dos valores informados está fora da faixa permitida para o campo.";
            case 3819:
            case 4025:
                return "Um dos valores informados é inválido para este campo.";
            case 1045:
            case 1044:
                return "Sem permissão de acesso ao banco de dados.";
            default:
                break;
        }
        String sqlState = erro.getSQLState();
        if (sqlState == null) {
            return null;
        }
        if (sqlState.startsWith("08")) {
            return "Não foi possível conectar ao banco de dados. Tente novamente em instantes.";
        }
        if (sqlState.startsWith("23")) {
            return "Operação não permitida: os dados violam uma regra de integridade (registro duplicado ou vinculado a outro).";
        }
        if (sqlState.startsWith("22")) {
            return "Um dos valores informados é inválido para o campo correspondente.";
        }
        return null;
    }
}
