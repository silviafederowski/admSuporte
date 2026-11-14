<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Conteudo da janela de log (LogsServlet): so a tabela, carregada dentro do <dialog> de logsIcone.jspf. --%>
<c:choose>
    <c:when test="${not empty formError}">
        <p class="form-error">${formError}</p>
    </c:when>
    <c:when test="${empty logs}">
        <p class="empty-state">Nenhuma operação registrada.</p>
    </c:when>
    <c:otherwise>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th style="width:120px;">Data e hora</th>
                    <th style="width:110px;">Usuário</th>
                    <th style="width:120px;">Tela</th>
                    <th style="text-align:left;">Operação</th>
                    <th style="text-align:left;">Alteração (antes → depois)</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="l" items="${logs}">
                    <tr>
                        <td>${l.dataHoraFormatada}</td>
                        <td><c:out value="${l.usuario}" /></td>
                        <td><c:out value="${l.tela}" /></td>
                        <td style="text-align:left;"><c:out value="${l.operacao}" /></td>
                        <td style="text-align:left;" class="log-alteracao"><c:out value="${empty l.alteracao ? '-' : l.alteracao}" /></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <p class="field-hint">${logs.size()} registro(s)${logs.size() >= 500 ? ' (mostrando os 500 mais recentes)' : ''}.</p>
    </c:otherwise>
</c:choose>
