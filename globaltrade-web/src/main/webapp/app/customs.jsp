<%@ page contentType="text/html;charset=UTF-8" %>
<% if (request.isUserInRole("VENDOR_USER")) { response.sendError(403); return; } %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Trade Clearance</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">INTERNATIONAL COMPLIANCE FLOW</p><h1>Trade Clearance</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="clearance-banner"><div><span class="hero-chip">Cross-border readiness</span><h2>Keep documentation moving before deadlines arrive.</h2></div><% if (request.isUserInRole("ADMIN") || request.isUserInRole("CUSTOMS_OFFICER")) { %><button class="secondary-action" id="reviewDeadlines">Review due items</button><% } %></div>
<article class="panel table-panel"><div class="panel-head"><div><p class="panel-kicker">DOCUMENT FLOW</p><h3>Clearance register</h3></div><span class="soft-chip" id="customsCount">0 documents</span></div>
<div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Shipment</th><th>Route</th><th>Type</th><th>Deadline</th><th>Status</th><th></th></tr></thead><tbody id="customsTable"></tbody></table></div></article>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/customs.js"></script>
</body>
</html>
