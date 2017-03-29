<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Europeana Search & Record API version</title>
</head>

<body>

    <div class="container">

        <p>API     : <c:out value="${versionInfo.getApiBuildInfo()}" /></p>
        <p>Corelib : <c:out value="${versionInfo.getCorelibBuildInfo()}" /></p>

    </div>

</body>
</html>