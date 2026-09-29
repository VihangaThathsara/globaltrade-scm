<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Partners</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">SUPPLIER NETWORK</p><h1>Partners</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

            <div class="toolbar-card">
                <div class="search-box"><span>⌕</span><input id="vendorSearch" aria-label="Search partner, code or country"></div>
                <% if (request.isUserInRole("ADMIN") || request.isUserInRole("LOGISTICS_COORDINATOR")) { %>
                <button class="primary-action" data-open-modal="vendorModal">+ New partner</button>
                <% } %>
            </div>

            <div class="partner-grid" id="partnerGrid"
                 data-can-manage="<%= request.isUserInRole("ADMIN") %>"></div>

            <div class="modal" id="vendorModal" data-reset-form>
                <div class="modal-card">
                    <div class="modal-head">
                        <div><p class="panel-kicker">NEW PARTNER</p><h3>Add network partner</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <form id="vendorForm" class="form-grid">
                        <label><span>Partner code</span><input name="vendorCode" required></label>
                        <label><span>Company name</span><input name="name" required></label>
                        <label><span>Country</span><input name="country" required></label>
                        <label><span>Operations email</span><input name="contactEmail" type="email"></label>
                        <div class="form-actions span-2">
                            <button type="button" class="secondary-action" data-close-modal>Cancel</button>
                            <button class="primary-action">Add partner</button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="modal" id="reviewHistoryModal">
                <div class="modal-card modal-card-wide">
                    <div class="modal-head">
                        <div><p class="panel-kicker">REVIEW HISTORY</p><h3 id="reviewHistoryTitle">Partner reviews</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <div class="review-history-list" id="reviewHistoryList"></div>
                </div>
            </div>

            <div class="modal" id="deactivatePartnerModal">
                <div class="modal-card modal-card-compact">
                    <div class="modal-head">
                        <div><p class="panel-kicker danger-kicker">DEACTIVATE PARTNER</p><h3>Deactivate partner?</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <p class="modal-copy">This partner will be unavailable for new inventory and shipments. Existing stock, shipment records, reviews and audit history will remain available.</p>
                    <input type="hidden" id="deactivatePartnerId">
                    <div class="form-actions">
                        <button type="button" class="secondary-action" data-close-modal>Cancel</button>
                        <button type="button" class="danger-action" id="confirmDeactivatePartner">Deactivate partner</button>
                    </div>
                </div>
            </div>
        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/vendors.js"></script>
</body>
</html>
