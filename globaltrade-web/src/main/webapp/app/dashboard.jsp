<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Dashboard</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">GLOBAL NETWORK CONTROL</p></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="hero-band">
    <div>
        <span class="hero-chip"><span class="live-dot"></span> Live network view</span>
        <h2>Keep every movement in sight.</h2>
        <p>Current shipment flow, inventory readiness and trade activity across the network.</p>
    </div>
    <div class="hero-route-art"><span></span><span></span><span></span></div>
</div>
<div class="stats-grid" id="statsGrid">
    <div class="stat-card skeleton"></div><div class="stat-card skeleton"></div><div class="stat-card skeleton"></div><div class="stat-card skeleton"></div><div class="stat-card skeleton"></div>
</div>
<div class="dashboard-grid">
    <article class="panel panel-wide">
        <div class="panel-head"><div><p class="panel-kicker">FLOW</p><h3>Shipment distribution</h3></div><span class="soft-chip">Current network</span></div>
        <div class="status-bars" id="statusBars"></div>
    </article>
    <article class="panel">
        <div class="panel-head"><div><p class="panel-kicker">ATTENTION</p><h3>Priority alerts</h3></div><a class="text-link" href="alerts.jsp">View all</a></div>
        <div class="alert-list" id="dashboardAlerts"></div>
    </article>
</div>
<article class="panel table-panel">
    <div class="panel-head"><div><p class="panel-kicker">RECENT MOVEMENTS</p><h3>Latest shipments</h3></div><a class="text-link" href="shipments.jsp">Open shipments</a></div>
    <div class="table-wrap"><table class="data-table"><thead><tr><th>Tracking</th><th>Route</th><th>Carrier</th><th>Warehouse</th><th>ETA</th><th>Status</th></tr></thead><tbody id="recentShipments"></tbody></table></div>
</article>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/dashboard.js"></script>
</body>
</html>
