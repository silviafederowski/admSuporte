package br.com.silsys.admsuporte.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monta o texto da coluna operacoes_log.alteracao numa edicao: uma linha "Campo: antes → depois" por campo, so com
 * os campos que mudaram. Compara o registro lido do banco antes e depois de salvar, propriedade a
 * propriedade (get/set do JavaBean), entao serve para qualquer tela sem codigo especifico.
 * Ficam de fora: id/chave, senhas, listas, e o id de um relacionamento quando existe o nome dele
 * (ex.: prestadorId, mostrado como prestadorNome). Nunca lanca excecao: na falha devolve null, para
 * o log nunca impedir a gravacao.
 */
public final class Alteracoes {

    private static final Logger LOGGER = Logger.getLogger(Alteracoes.class.getName());

    private static final Set<String> IGNORADOS = new HashSet<>(Arrays.asList(
            "id", "chave", "createdAt", "trocarSenha", "userTypeNivel", "ultimoPrestadorClassificacao"));

    private static final List<String> SUFIXOS_NOME = Arrays.asList("Nome", "Name", "Descricao");

    private static final Map<String, String> ROTULOS = new HashMap<>();

    static {
        ROTULOS.put("descricao", "Descrição");
        ROTULOS.put("observacao", "Observação");
        ROTULOS.put("nomeRazaoSocial", "Nome / razão social");
        ROTULOS.put("email", "E-mail");
        ROTULOS.put("phone", "Telefone");
        ROTULOS.put("name", "Nome");
        ROTULOS.put("ativo", "Ativo");
        ROTULOS.put("userTypeName", "Tipo de usuário");
        ROTULOS.put("classificacao", "Classificação");
        ROTULOS.put("tipo", "Tipo");
        ROTULOS.put("periodicidade", "Periodicidade");
        ROTULOS.put("unidadePeriodicidade", "Unidade da periodicidade");
        ROTULOS.put("ultimaExecucao", "Última execução");
        ROTULOS.put("ultimoPrestadorNome", "Último prestador");
        ROTULOS.put("valorPagoUltimaExecucao", "Valor pago na última execução");
        ROTULOS.put("data", "Data");
        ROTULOS.put("hora", "Hora");
        ROTULOS.put("pontoFocal", "Ponto focal");
        ROTULOS.put("assunto", "Assunto");
        ROTULOS.put("servicoDescricao", "Serviço");
        ROTULOS.put("prestadorNome", "Prestador");
        ROTULOS.put("status", "Status");
        ROTULOS.put("dataStatus", "Data do status");
        ROTULOS.put("tipoDescricao", "Tipo");
        ROTULOS.put("unidade", "Unidade");
        ROTULOS.put("conversaoUnidades", "Conversão de unidades");
        ROTULOS.put("estoqueIdeal", "Estoque ideal");
        ROTULOS.put("estoqueMinimo", "Estoque mínimo");
        ROTULOS.put("estoqueAtual", "Estoque atual");
        ROTULOS.put("comprar", "Comprar");
        ROTULOS.put("ultimoFornecedorNome", "Último fornecedor");
        ROTULOS.put("valorUltimaCompra", "Valor da última compra");
        ROTULOS.put("codigo", "Código");
        ROTULOS.put("quantasVagas", "Quantas vagas");
        ROTULOS.put("candidataIdoso", "Candidata a vaga de idoso");
        ROTULOS.put("vagasGaragem", "Vagas na garagem");
        ROTULOS.put("telaCodigo", "Tela");
        ROTULOS.put("telaDescricao", "Descrição da tela");
        ROTULOS.put("nivel", "Nível");
        ROTULOS.put("tipoAcesso", "Tipo de acesso");
    }

    private Alteracoes() {
    }

    /** Texto das diferencas entre antes e depois (mesma classe), ou null se nada mudou / falhou. */
    public static String descrever(Object antes, Object depois) {
        if (antes == null || depois == null || antes.getClass() != depois.getClass()) {
            return null;
        }
        try {
            BeanInfo info = Introspector.getBeanInfo(antes.getClass(), Object.class);
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            Set<String> nomes = new HashSet<>();
            for (PropertyDescriptor p : props) {
                nomes.add(p.getName());
            }
            StringBuilder texto = new StringBuilder();
            for (PropertyDescriptor p : props) {
                Method get = p.getReadMethod();
                if (get == null || p.getWriteMethod() == null || ignorar(p, nomes)) {
                    continue;
                }
                String valorAntes = formatar(get.invoke(antes));
                String valorDepois = formatar(get.invoke(depois));
                if (!Objects.equals(valorAntes, valorDepois)) {
                    adicionar(texto, rotulo(p.getName()), valorAntes, valorDepois);
                }
            }
            return texto.length() == 0 ? null : texto.toString();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Nao foi possivel descrever a alteracao para o log.", e);
            return null;
        }
    }

    /** Uma diferenca avulsa (ex.: lista de veiculos), no mesmo formato; null se antes e depois iguais. */
    public static String campo(String rotulo, String antes, String depois) {
        String a = vazioParaNull(antes);
        String d = vazioParaNull(depois);
        if (Objects.equals(a, d)) {
            return null;
        }
        StringBuilder texto = new StringBuilder();
        adicionar(texto, rotulo, a, d);
        return texto.toString();
    }

    /** Junta varias partes (ignorando as nulas), uma por linha, ou null se nenhuma. */
    public static String juntar(String... partes) {
        StringBuilder texto = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isEmpty()) {
                if (texto.length() > 0) {
                    texto.append('\n');
                }
                texto.append(parte);
            }
        }
        return texto.length() == 0 ? null : texto.toString();
    }

    private static void adicionar(StringBuilder texto, String rotulo, String antes, String depois) {
        if (texto.length() > 0) {
            texto.append('\n');
        }
        texto.append(rotulo).append(": ")
                .append(antes == null ? "(vazio)" : antes)
                .append(" → ")
                .append(depois == null ? "(vazio)" : depois);
    }

    private static boolean ignorar(PropertyDescriptor p, Set<String> nomes) {
        String nome = p.getName();
        Class<?> tipo = p.getPropertyType();
        if (IGNORADOS.contains(nome) || tipo == null || tipo.isArray() || Collection.class.isAssignableFrom(tipo)
                || Map.class.isAssignableFrom(tipo)) {
            return true;
        }
        String minusculo = nome.toLowerCase();
        if (minusculo.contains("password") || minusculo.contains("senha") || minusculo.contains("hash")
                || minusculo.contains("salt") || minusculo.contains("token")) {
            return true;
        }
        if (nome.endsWith("Id") && nome.length() > 2) {
            String prefixo = nome.substring(0, nome.length() - 2);
            for (String sufixo : SUFIXOS_NOME) {
                if (nomes.contains(prefixo + sufixo)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String formatar(Object valor) throws Exception {
        if (valor == null) {
            return null;
        }
        if (valor instanceof String) {
            return vazioParaNull((String) valor);
        }
        if (valor instanceof LocalDate) {
            return DateFormatUtil.formatar((LocalDate) valor);
        }
        if (valor instanceof LocalTime) {
            return DateFormatUtil.formatarHora((LocalTime) valor);
        }
        if (valor instanceof LocalDateTime) {
            LocalDateTime dataHora = (LocalDateTime) valor;
            return DateFormatUtil.formatar(dataHora.toLocalDate()) + " " + DateFormatUtil.formatarHora(dataHora.toLocalTime());
        }
        if (valor instanceof BigDecimal) {
            return ((BigDecimal) valor).stripTrailingZeros().toPlainString();
        }
        if (valor instanceof Boolean) {
            return (Boolean) valor ? "Sim" : "Não";
        }
        if (valor instanceof Enum) {
            try {
                Method label = valor.getClass().getMethod("getLabel");
                return String.valueOf(label.invoke(valor));
            } catch (NoSuchMethodException e) {
                return ((Enum<?>) valor).name();
            }
        }
        return String.valueOf(valor);
    }

    private static String vazioParaNull(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    /** "contato1Telefone" -> "Contato 1 telefone" quando nao ha rotulo cadastrado. */
    private static String rotulo(String propriedade) {
        String rotulo = ROTULOS.get(propriedade);
        if (rotulo != null) {
            return rotulo;
        }
        String separado = propriedade
                .replaceAll("([a-z])([A-Z0-9])", "$1 $2")
                .replaceAll("([0-9])([A-Za-z])", "$1 $2")
                .toLowerCase();
        return Character.toUpperCase(separado.charAt(0)) + separado.substring(1);
    }
}
