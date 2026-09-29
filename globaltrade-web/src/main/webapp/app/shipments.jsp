<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Shipments</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">GLOBAL MOVEMENT</p><h1>Shipments</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="toolbar-card">
    <div class="search-box"><span>⌕</span><input id="shipmentSearch" aria-label="Search tracking, route, carrier, product or warehouse"></div>
    <select class="filter-select" id="shipmentStatusFilter"><option value="">All statuses</option><option>CREATED</option><option>READY</option><option>IN_TRANSIT</option><option>DELAYED</option><option>DELIVERED</option><option>CANCELLED</option></select>
    <% if (request.isUserInRole("ADMIN") || request.isUserInRole("LOGISTICS_COORDINATOR")) { %>
    <button class="primary-action" data-open-modal="shipmentModal">+ New shipment</button>
    <% } %>
</div>
<article class="panel table-panel">
    <div class="panel-head"><div><p class="panel-kicker">MOVEMENT REGISTER</p><h3>Shipment network</h3></div><span class="soft-chip" id="shipmentCount">0 records</span></div>
    <div class="table-wrap"><table class="data-table"><thead><tr><th>Tracking</th><th>Route</th><th>Carrier</th><th>Warehouse</th><th>Cargo</th><th>ETA</th><th>Clearance</th><th>Route health</th><th>Status</th><th>Action</th></tr></thead><tbody id="shipmentTable"></tbody></table></div>
</article>
<div class="modal" id="shipmentModal" data-reset-form><div class="modal-card modal-lg"><div class="modal-head"><div><p class="panel-kicker">NEW MOVEMENT</p><h3>Create shipment</h3></div><button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button></div>
<form id="shipmentForm" class="form-grid">
<label class="span-2"><span>Product</span><select name="productName" id="shipmentProduct" required></select></label>
<label><span>Quantity</span><input name="quantity" type="number" min="1" required></label>
<label><span>Carrier</span><input name="carrier" required></label>
<label><span>Origin</span><input name="origin" required></label>
<label><span>Destination</span><input name="destination" required></label>
<label class="span-2"><span>Expected arrival</span><input name="eta" type="datetime-local" required></label>
<div class="form-actions span-2"><button type="button" class="secondary-action" data-close-modal>Cancel</button><button class="primary-action" type="submit">Create shipment</button></div>
</form></div></div>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/shipments.js"></script>
</body>
</html>
