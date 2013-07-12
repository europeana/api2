<%@ page session="false" language="java" contentType="application/rdf+xml; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" trimDirectiveWhitespaces="true" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%>
<c:choose>
  <c:when test="${!empty error}"><error>${error}</error></c:when>
  <c:otherwise>${record}</c:otherwise>
</c:choose>