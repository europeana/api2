<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>API2 DEMO - User Favorties</title>
<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>" />
</head>

<body>
<c:import url="../links.jsp" />
<h1>Social Tags for: ${username}</h1>
<table border="1">
<tr>
	<td>title</td>
	<td>tag</td>
	<td>date</td>
	<td>action</td>
</tr>

<c:forEach var="tag" items="${items}"> 
	<tr>
		<td>${tag.title}</td>
		<td>${tag.tag}</td>
		<td><fmt:formatDate value="${tag.dateSaved}"/></td>
		<td>
			<a href="${fn:replace(tag.europeanaUri, 'www.europeana.eu/resolve', 'localhost:8081/portal')}.html" target="_blank">view</a>
			<a href="tags?action=DELETE&id=${tag.id}">delete</a>
		</td>
	</tr>
</c:forEach>
</table>
</body>
</html>