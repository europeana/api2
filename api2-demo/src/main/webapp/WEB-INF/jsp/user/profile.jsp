<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>API2 DEMO - ${profile.userName}</title>
<link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>" />
</head>

<body>
<h1>Welcome ${profile.firstName} ${profile.lastName}</h1>
<p>Is your email address still ${profile.email}?</p>
<p/>
<p>Saved Items: ${profile.nrOfSavedItems}</p>
<p>Saved Searches: ${profile.nrOfSavedSearches}</p>
<p>Social Tags: ${profile.nrOfSocialTags}</p>
<p/>
<p/>
<p/>
<c:import url="../links.jsp" />
</body>