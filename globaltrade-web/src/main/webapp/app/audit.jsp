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
    <title>GlobalTrade | Activity Trail</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">OPERATIONAL ACCOUNTABILITY</p><h1>Activity Trail</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

<div class="toolbar-card"><div class="search-box"><span>⌕</span><input id="auditSearch" aria-label="Search user, action or area"></div><button class="secondary-action" id="refreshAudit">Refresh</button></div>
<article class="panel audit-panel"><div class="panel-head"><div><p class="panel-kicker">RECENT ACTIVITY</p><h3>Operations history</h3></div><span class="soft-chip" id="auditCount">0 shown</span></div>
<div class="audit-list" id="auditList"></div></article>

        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/audit.js"></script>
</body>
</html>
