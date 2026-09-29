<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String uri = request.getRequestURI();
    String ctx = request.getContextPath();
    boolean adminRole = request.isUserInRole("ADMIN");
    boolean logisticsRole = request.isUserInRole("LOGISTICS_COORDINATOR");
    boolean warehouseRole = request.isUserInRole("WAREHOUSE_MANAGER");
    boolean customsRole = request.isUserInRole("CUSTOMS_OFFICER");
    boolean vendorRole = request.isUserInRole("VENDOR_USER");
%>
<aside class="sidebar" id="sidebar">
    <div class="brand-block">
        <div class="brand-mark-wrap"><img src="<%=ctx%>/assets/images/globaltrade-logo.png" alt="GlobalTrade logo"></div>
        <div class="brand-copy">
            <strong>GlobalTrade</strong>
            <span>Supply Chain</span>
        </div>
    </div>

    <nav class="side-nav">
        <div class="nav-label">Overview</div>
        <a class="nav-item <%= uri.endsWith("dashboard.jsp") ? "active" : "" %>" href="<%=ctx%>/app/dashboard.jsp">
            <span class="nav-icon">⌂</span><span>Dashboard</span>
        </a>

        <div class="nav-label">Operations</div>
        <a class="nav-item <%= uri.endsWith("shipments.jsp") ? "active" : "" %>" href="<%=ctx%>/app/shipments.jsp">
            <span class="nav-icon">↗</span><span>Shipments</span>
        </a>
        <a class="nav-item <%= uri.endsWith("inventory.jsp") ? "active" : "" %>" href="<%=ctx%>/app/inventory.jsp">
            <span class="nav-icon">▦</span><span>Inventory</span>
        </a>
        <a class="nav-item <%= uri.endsWith("vendors.jsp") ? "active" : "" %>" href="<%=ctx%>/app/vendors.jsp">
            <span class="nav-icon">◇</span><span>Partners</span>
        </a>
        <% if (!vendorRole) { %>
        <a class="nav-item <%= uri.endsWith("customs.jsp") ? "active" : "" %>" href="<%=ctx%>/app/customs.jsp">
            <span class="nav-icon">▤</span><span>Trade Clearance</span>
        </a>
        <% } %>

        <div class="nav-label">Awareness</div>
        <a class="nav-item <%= uri.endsWith("alerts.jsp") ? "active" : "" %>" href="<%=ctx%>/app/alerts.jsp">
            <span class="nav-icon">!</span><span>Alerts</span>
        </a>
        <% if (adminRole || logisticsRole) { %>
        <a class="nav-item <%= uri.endsWith("audit.jsp") ? "active" : "" %>" href="<%=ctx%>/app/audit.jsp">
            <span class="nav-icon">◎</span><span>Activity Trail</span>
        </a>
        <a class="nav-item <%= uri.endsWith("monitor.jsp") ? "active" : "" %>" href="<%=ctx%>/app/monitor.jsp">
            <span class="nav-icon">⌁</span><span>Operations Health</span>
        </a>
        <% } %>
    </nav>

</aside>
