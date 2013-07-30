<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>API2 DEMO - User Saved Searches</title>
<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>" />
</head>

<body>
<c:import url="../links.jsp" />
<h1>Saved Searches for: ${username}</h1>
<table border="1">
<tr>
	<td>query</td>
	<td>date</td>
	<td>action</td>
</tr>

<c:forEach var="search" items="${items}"> 
	<tr>
		<td>${search.query}</td>
		<td><fmt:formatDate value="${search.dateSaved}"/></td>
		<td>
			<a href="http://www.europeana.eu/portal/search.html?rows=24&query=${search.queryString}" target="_blank">view</a>
			<a href="searches?action=DELETE&id=${search.id}">delete</a>
		</td>
	</tr>
</c:forEach>
</table>
</body>
</html>