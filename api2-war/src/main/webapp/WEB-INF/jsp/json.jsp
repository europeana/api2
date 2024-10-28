<%@ page session="false" language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
  <c:when test="${!empty error}">{"error": ${error}}</c:when>
  <c:otherwise>${json}</c:otherwise>
</c:choose>