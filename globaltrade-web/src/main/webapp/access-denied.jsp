<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head><title>GlobalTrade | Access restricted</title><%@ include file="/WEB-INF/includes/head.jsp" %></head>
<body class="login-body">
<div class="login-grid-bg"></div>
<main class="center-state">
    <div class="state-card">
        <span class="state-icon">↯</span>
        <h1>Access restricted</h1>
        <p>Your current operations role does not include this workspace.</p>
        <a class="primary-action" href="<%=request.getContextPath()%>/app/dashboard.jsp">Return to dashboard</a>
    </div>
</main>
</body>
</html>
