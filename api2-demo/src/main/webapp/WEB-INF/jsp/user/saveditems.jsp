<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
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