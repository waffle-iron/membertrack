<%@ tag description="Master page, template for other pages" 
        pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="sidebar" type="java.lang.Boolean" %>
<%@ attribute name="title" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="js" fragment="true" %>

<c:url value="/static/css/pure.css"         var="pureUrl" />
<c:url value="/static/css/pure-theme.css"   var="pureThemeUrl" />
<c:url value="/static/css/membertrack.css"  var="masterCssUrl" />

<!DOCTYPE html>
<html class="pure-theme-membertrack">
    <head>
        <title>${title}</title>
        <meta charset="utf-8" />
        <link rel="stylesheet" href="${pureUrl}" />
        <link rel="stylesheet" href="${pureThemeUrl}" />
        <link rel="stylesheet" href="${masterCssUrl}" />
        <jsp:invoke fragment="css" />
    </head>
    <body class="l-page">
        <div class="l-main-area">
            <div class="l-content">
                <jsp:doBody />
            </div>
        </div>
        <c:if test="${empty sidebar or sidebar}">
            <div class="l-sidebar">
            </div>
        </c:if>
        <jsp:invoke fragment="js" />
    </body>
</html>