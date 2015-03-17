<%@ page session="false" language="java" contentType="charset=UTF-8" pageEncoding="UTF-8"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Europeana API Login</title>
<link type="text/css" rel="stylesheet"
	href="<c:url value="/style.css"/>" />
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

		<p>Use your <strong>MyEuropeana account</strong> to login here!</p>
		<form id="loginForm" name="loginForm"
			action="<c:url value="login.do"/>" method="post">
			<p>
				<label>E-mail: <input type='text' name='j_username'/></label>
			</p>
			<p>
				<label>Password: <input type="password" name='j_password'/></label>
			</p>

			<p>
				<input name="login" value="Login" type="submit"/>
			</p>
		</form>
	</div>

</body>
</html>
