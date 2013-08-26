<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>API2 DEMO - User Saved Items</title>
<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>" />
</head>

<body>
<c:import url="../links.jsp" />
<h1>Saved Items for: ${username}</h1>
<table border="1">
<tr>
	<td>title</td>
	<td>author</td>
	<td>date</td>
	<td>action</td>
</tr>

<c:forEach var="item" items="${items}"> 
	<tr>
		<td>${item.title}</td>
		<td>${item.author}</td>
		<td><fmt:formatDate value="${item.dateSaved}"/></td>
		<td>
			<a href="${item.guid}" target="_blank">view</a>
			<a href="saveditems?action=DELETE&id=${item.id}">delete</a>
		</td>
	</tr>
</c:forEach>
</table>
</body>
</html>