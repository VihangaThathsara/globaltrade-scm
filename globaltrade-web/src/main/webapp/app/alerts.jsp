<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Alerts</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">NETWORK AWARENESS</p><h1>Alerts</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="alert-summary" id="alertSummary"></div>
<article class="panel"><div class="panel-head"><div><p class="panel-kicker">OPEN ATTENTION ITEMS</p><h3>Operational alerts</h3></div><span class="soft-chip" id="alertCount">0 open</span></div><div class="alert-center" id="alertCenter" data-can-resolve='<%= (request.isUserInRole("ADMIN") || request.isUserInRole("LOGISTICS_COORDINATOR") || request.isUserInRole("WAREHOUSE_MANAGER") || request.isUserInRole("CUSTOMS_OFFICER")) ? "true" : "false" %>'></div></article>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/alerts.js"></script>
</body>
</html>
