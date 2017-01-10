<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

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

<%--<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"--%>
<%--"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">--%>
<%--<html xmlns="http://www.w3.org/1999/xhtml">--%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
        <title>Europeana Login</title>
    </head>
    <body>

        <h1>EUROPEANA.EU API</h1>

        <div id="content">
            <c:if test="${not empty param.authentication_error}">
                <h1>Woops!</h1>

                <p class="error">Your login attempt was not successful.</p>
            </c:if>
            <c:if test="${not empty param.authorization_error}">
                <h1>Woops!</h1>

                <p class="error">You are not permitted to access that resource.</p>
            </c:if>

            <h2>Login</h2>

            <p>Use your
                <strong>MyEuropeana account</strong>
                to login here!
            </p>
            <form id="loginForm" name="loginForm"
                  action="<c:url value="${base}oAuthLogin.do"/>" method="post">
                <input type="hidden" name="${_csrf.parameterName}"
                       value="${_csrf.token}" />
                <p>
                    <label>E-mail:
                        <input type="text" name="username" id="username"/>
                    </label>
                </p>
                <p>
                    <label>Password:
                        <input type="password" name="password" id="password"/>
                    </label>
                </p>

                <p>
                    <input name="login" value="Login" type="submit"/>
                </p>
            </form>
        </div>

    </body>
</html>
