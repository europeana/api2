<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~ Copyright 2007-2015 The Europeana Foundation
  ~
  ~ Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  ~ by the European Commission;
  ~ You may not use this work except in compliance with the Licence.
  ~
  ~ You may obtain a copy of the Licence at:
  ~ http://joinup.ec.europa.eu/software/page/eupl
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed under
  ~ the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  ~ any kind, either express or implied.
  ~ See the Licence for the specific language governing permissions and limitations under
  ~ the Licence.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Europeana API</title>
	<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>"/>
</head>

<body>

	<h1>EUROPEANA.EU API</h1>

	<div id="content">

		<% if (session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null && !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) instanceof UnapprovedClientAuthenticationException)) { %>
		<div class="error">
			<h2>Woops!</h2>

			<p>Access could not be granted. (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)</p>
		</div>
		<% } %>
		<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />

		<authz:authorize ifAllGranted="ROLE_USER">
			<h2>Please Confirm</h2>

			<p>You hereby authorize "<c:out value="${appName}" />" to access your:</p>
			<ul>
				<li>MyEuropeana profile data</li>
				<li>Saved items</li>
				<li>Tags</li>
				<li>Saved searches</li>
			</ul>

			<form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
				<input name="user_oauth_approval" value="true" type="hidden" />
				<label><input name="authorize" value="Authorize" type="submit"></label>
			</form>
			<form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
				<input name="user_oauth_approval" value="false" type="hidden" />
				<label><input name="deny" value="Deny" type="submit"></label>
			</form>
		</authz:authorize>
	</div>

</body>
</html>
