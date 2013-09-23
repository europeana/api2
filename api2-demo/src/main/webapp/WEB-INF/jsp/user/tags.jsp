<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>API2 DEMO - User Favorties</title>
<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>" />
</head>

<body>
<c:import url="../links.jsp" />
<h1>Social Tags for: ${username}</h1>
<table border="1">
	<thead>
	<tr>
		<th>title</th>
		<th>tag</th>
		<th>date</th>
		<th>action</th>
	</tr>
	</thead>
	<tbody>
<c:forEach var="tag" items="${items}"> 
	<tr>
		<td>${tag.title}</td>
		<td>${tag.tag}</td>
		<td><fmt:formatDate value="${tag.dateSaved}"/></td>
		<td>
			<a href="${tag.guid}" target="_blank">view</a>
			<a href="tags?action=DELETE&id=${tag.id}">delete</a>
		</td>
	</tr>
</c:forEach>
	</tbody>
</table>

</body>
</html>