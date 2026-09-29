<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (!(request.isUserInRole("ADMIN") || request.isUserInRole("LOGISTICS_COORDINATOR"))) {
        response.sendError(403);
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Operations Health</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">NETWORK RELIABILITY</p><h1>Operations Health</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="health-hero"><div><span class="hero-chip"><span class="live-dot"></span> Control plane active</span><h2>Background operations are keeping the network current.</h2><p>Latest operational checks and service response observations.</p></div><div class="health-ring"><div><strong id="healthScore">--</strong><span>health</span></div></div></div>
<div class="dashboard-grid"><article class="panel"><div class="panel-head"><div><p class="panel-kicker">AUTOMATED CHECKS</p><h3>Operational routines</h3></div></div><div class="automation-list" id="automationList"></div></article>
<article class="panel panel-wide"><div class="panel-head"><div><p class="panel-kicker">RESPONSE OBSERVATIONS</p><h3>Recent service timings</h3></div></div><div class="metric-list" id="metricList"></div></article></div>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/monitor.js"></script>
</body>
</html>
