<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Europeana API</title>
</head>

<body>

<h1>EUROPEANA.EU API</h1>
<c:set var="context" value="${pageContext.request.contextPath}" />
<div id="content">

    <% if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null && !(session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof UnapprovedClientAuthenticationException)) { %>
    <div class="error">
        <h2>Woops!</h2>

        <p>Access could not be granted.
            (<%= ((AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)).getMessage() %>
            )</p>
    </div>
    <% } %>


    <authz:authorize access="hasRole('ROLE_USER')">
        <h2>Please Confirm</h2>

        <p>You hereby authorize "<c:out value="${appName}"/>" to access your:</p>
        <ul>
            <li>MyEuropeana profile data</li>
            <li>Saved items</li>
            <li>Tags</li>
            <li>Saved searches</li>
        </ul>

        <form:form id="confirmationForm" name="confirmationForm" action="${context}/oauth/authorize"
              method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <input name="user_oauth_approval" value="true" type="hidden"/>
            <c:forEach items="${scopes}" var="scope">
                <input name="${scope.key}" value="true" type="hidden"/>
            </c:forEach>
            <label><input name="authorize" value="Authorize" type="submit"/></label>
        </form:form>
        <%--<form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">--%>
            <%--<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>--%>
            <%--<input name="user_oauth_approval" value="true" type="hidden"/>--%>
            <%--<c:forEach items="${scopes}" var="scope">--%>
                <%--<input name="${scope.key}" value="false" type="hidden"/>--%>
            <%--</c:forEach>--%>
            <%--<label><input name="deny" value="Deny" type="submit"></label>--%>
        <%--</form>--%>
    </authz:authorize>
</div>

</body>
</html>
