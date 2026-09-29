<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>GlobalTrade | Inventory</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <%@ include file="/WEB-INF/includes/sidebar.jsp" %>
    <main class="main-shell">
        <%@ include file="/WEB-INF/includes/topbar.jsp" %>
        <section class="page-content">
            <div class="page-heading">
                <div><p class="eyebrow">WAREHOUSE READINESS</p><h1>Inventory</h1></div>
                <div class="page-heading-actions" id="pageActions"></div>
            </div>

            <div class="mini-stats" id="inventoryStats"></div>
            <div class="toolbar-card">
                <div class="search-box"><span>⌕</span><input id="inventorySearch" aria-label="Search SKU, item, warehouse or supplier"></div>
                <% if (request.isUserInRole("ADMIN") || request.isUserInRole("WAREHOUSE_MANAGER")) { %>
                <button class="primary-action" data-open-modal="inventoryModal">+ Add item</button>
                <% } %>
            </div>

            <article class="panel table-panel inventory-panel">
                <div class="panel-head">
                    <div><p class="panel-kicker">STOCK POSITION</p><h3>Stock history</h3></div>
                    <span class="soft-chip" id="inventoryCount">0 stock records</span>
                </div>
                <div class="table-wrap">
                    <table class="data-table inventory-table" id="inventoryDataTable"
                           data-can-adjust="<%= request.isUserInRole("ADMIN") || request.isUserInRole("WAREHOUSE_MANAGER") %>"
                           data-can-review="<%= request.isUserInRole("ADMIN") || request.isUserInRole("WAREHOUSE_MANAGER") || request.isUserInRole("LOGISTICS_COORDINATOR") %>">
                        <thead><tr><th>SKU</th><th>Item</th><th>Warehouse</th><th>Received from</th><th>Received Qty</th><th>Received</th><th>Condition</th><th>Actions</th></tr></thead>
                        <tbody id="inventoryTable"></tbody>
                    </table>
                </div>
            </article>

            <div class="modal" id="inventoryModal" data-reset-form>
                <div class="modal-card">
                    <div class="modal-head">
                        <div><p class="panel-kicker">NEW STOCK ITEM</p><h3>Add inventory</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <form id="inventoryForm" class="form-grid">
                        <label><span>SKU</span><input name="sku" id="inventorySku" required></label>
                        <label><span>Item name</span><input name="itemName" id="inventoryItemName" required></label>
                        <label><span>Quantity</span><input name="quantity" type="number" min="1" required></label>
                        <label><span>Product preferred minimum</span><input name="reorderLevel" id="inventoryReorderLevel" type="number" min="0" required></label>
                        <label><span>Warehouse</span><select name="warehouseId" id="inventoryWarehouse" required></select></label>
                        <label><span>Received from supplier</span><select name="vendorId" id="inventoryVendor" required></select></label>
                        <div class="form-actions span-2">
                            <button type="button" class="secondary-action" data-close-modal>Cancel</button>
                            <button class="primary-action">Add inventory</button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="modal" id="inventoryAdjustModal" data-reset-form>
                <div class="modal-card modal-card-compact">
                    <div class="modal-head">
                        <div><p class="panel-kicker">STOCK UPDATE</p><h3 id="inventoryAdjustTitle">Adjust stock</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <div class="inventory-adjust-context" id="inventoryAdjustContext"></div>
                    <form id="inventoryAdjustForm" class="form-grid">
                        <input type="hidden" name="inventoryItemId">
                        <label><span>Adjustment</span><select name="adjustmentType" id="inventoryAdjustmentType"><option value="ADD">Add stock</option><option value="REMOVE">Remove stock</option></select></label>
                        <label><span>Units</span><input name="units" type="number" min="1" step="1" value="1" required></label>
                        <div class="form-actions span-2">
                            <button type="button" class="secondary-action" data-close-modal>Cancel</button>
                            <button class="primary-action">Update stock</button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="modal" id="supplyReviewModal" data-reset-form>
                <div class="modal-card supply-review-modal-card">
                    <div class="modal-head">
                        <div><p class="panel-kicker">PARTNER REVIEW</p><h3 id="supplyReviewTitle">Review partner</h3></div>
                        <button class="modal-close" type="button" aria-label="Close" data-close-modal><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button>
                    </div>
                    <div class="supply-review-context" id="supplyReviewContext"></div>
                    <form id="supplyReviewForm" class="form-grid">
                        <input type="hidden" name="inventoryItemId">
                        <input type="hidden" name="rating" id="supplyRatingValue" value="5">
                        <label class="span-2">
                            <span>Partner rating</span>
                            <div class="rating-picker-row">
                                <div class="rating-picker" id="supplyStarPicker" role="slider" aria-label="Partner rating" aria-valuemin="1" aria-valuemax="5" aria-valuenow="5" tabindex="0"></div>
                                <strong class="rating-value" id="supplyRatingText">5 / 5</strong>
                            </div>
                        </label>
                        <label class="span-2"><span>Review note <em>(optional)</em></span><textarea name="notes" rows="4"></textarea></label>
                        <div class="form-actions span-2">
                            <button type="button" class="secondary-action" data-close-modal>Cancel</button>
                            <button class="primary-action">Save review</button>
                        </div>
                    </form>
                </div>
            </div>
        </section>
    </main>
</div>
<div class="toast-host" id="toastHost"></div>
<%@ include file="/WEB-INF/includes/scripts.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/inventory.js"></script>
</body>
</html>
