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

		<p>This page is only for testing, please use a POST http request to <strong>/api/v2/mydata/login.do</strong> with the following attributes:<br/>
		<li><strong>apikey</strong>: containing your public apikey</li>
		<li><strong>privatekey</strong>: containing your private key</li>
		</p>
		<form id="loginForm" name="loginForm"
			action="<c:url value="/v2/mydata/login.do"/>" method="post">
			<p>
				<label>Public api key: <input type='text' name='apikey'/></label>
			</p>
			<p>
				<label>Secret api key: <input type="password" name='privatekey'/></label>
			</p>

			<p>
				<input name="login" value="Login" type="submit"/>
			</p>
		</form>
	</div>

</body>
</html>
