<%@ page contentType="text/html;charset=UTF-8" %>
<%
    String currentUser = request.getUserPrincipal() == null ? "User" : request.getUserPrincipal().getName();
    String roleLabel = request.isUserInRole("ADMIN") ? "Administrator" :
            request.isUserInRole("LOGISTICS_COORDINATOR") ? "Logistics Coordinator" :
            request.isUserInRole("WAREHOUSE_MANAGER") ? "Warehouse Manager" :
            request.isUserInRole("CUSTOMS_OFFICER") ? "Customs Officer" : "Vendor Representative";
%>
<header class="topbar">
    <button class="icon-button mobile-only" id="menuToggle" aria-label="Open navigation">☰</button>
    <div class="topbar-spacer"></div>
    <a class="topbar-alert" id="topbarAlerts" href="<%=request.getContextPath()%>/app/alerts.jsp" aria-label="Alerts">
        <svg class="bell-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"></path>
            <path d="M10 21h4"></path>
        </svg>
        <span class="alert-indicator" id="alertIndicator" hidden>0</span>
    </a>
    <div class="user-menu">
        <div class="avatar"><%= currentUser.substring(0,1).toUpperCase() %></div>
        <div class="user-copy"><strong><%= currentUser %></strong><span><%= roleLabel %></span></div>
        <form method="post" action="<%=request.getContextPath()%>/logout">
            <button class="logout-button" type="submit">Sign out</button>
        </form>
    </div>
</header>
