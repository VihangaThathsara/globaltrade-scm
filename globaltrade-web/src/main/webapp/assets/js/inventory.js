(() => {
    let items = [];
    let suppliers = [];
    let warehouses = [];

    const dataTable = document.getElementById('inventoryDataTable');
    const canAdjust = dataTable?.dataset.canAdjust === 'true';
    const canReview = dataTable?.dataset.canReview === 'true';
    const table = document.getElementById('inventoryTable');
    const search = document.getElementById('inventorySearch');
    const skuInput = document.getElementById('inventorySku');
    const itemNameInput = document.getElementById('inventoryItemName');
    const warehouseSelect = document.getElementById('inventoryWarehouse');
    const supplierSelect = document.getElementById('inventoryVendor');
    const reorderInput = document.getElementById('inventoryReorderLevel');

    let selectedRating = 5;
    const ratingInput = document.getElementById('supplyRatingValue');
    const starPicker = document.getElementById('supplyStarPicker');
    const ratingText = document.getElementById('supplyRatingText');

    const formatRating = value => Number(value).toFixed(1).replace('.0', '');

    const paintStars = value => {
        if (!starPicker) return;
        starPicker.querySelectorAll('.rating-star').forEach((button, index) => {
            const amount = Math.max(0, Math.min(1, Number(value) - index));
            const fill = button.querySelector('.rating-star-fill-clip');
            if (fill) fill.style.width = `${amount * 100}%`;
        });
    };

    const setRating = value => {
        selectedRating = Math.max(1, Math.min(5, Math.round(Number(value))));
        if (ratingInput) ratingInput.value = String(selectedRating);
        paintStars(selectedRating);
        if (ratingText) ratingText.textContent = `${formatRating(selectedRating)} / 5`;
    };

    const setupStarPicker = () => {
        if (!starPicker) return;
        starPicker.innerHTML = Array.from({length: 5}, (_, index) => `
            <button type="button" class="rating-star" data-star="${index + 1}" aria-label="${index + 1} star">
                <span class="rating-star-base">★</span>
                <span class="rating-star-fill-clip"><span class="rating-star-fill">★</span></span>
            </button>`).join('');
        starPicker.querySelectorAll('.rating-star').forEach(button => {
            button.addEventListener('click', () => {
                const star = Number(button.dataset.star);
                setRating(star);
            });
        });
        setRating(5);
    };

    const productCode = name => {
        const cleaned = String(name || '').toUpperCase().replace(/[^A-Z0-9 ]/g, ' ').trim();
        if (!cleaned) return 'ITEM';
        const first = cleaned.split(/\s+/).filter(Boolean)[0] || 'ITEM';
        return first.slice(0, 4).padEnd(4, 'X');
    };

    const syncProductMinimum = () => {
        if (!itemNameInput || !reorderInput) return;
        const name = itemNameInput.value.trim().toLowerCase();
        const existing = items.find(i => String(i.itemName || '').trim().toLowerCase() === name);
        if (existing) {
            reorderInput.value = String(Number(existing.reorderLevel || 0));
            reorderInput.readOnly = true;
            reorderInput.dataset.productMinimum = 'true';
        } else {
            if (reorderInput.dataset.productMinimum === 'true') reorderInput.value = '';
            reorderInput.readOnly = false;
            delete reorderInput.dataset.productMinimum;
        }
    };
    const suggestSku = () => {
        if (!skuInput || !itemNameInput) return;
        const name = itemNameInput.value.trim();
        if (!name) return;
        if (skuInput.value.trim() && skuInput.dataset.suggested !== 'true') return;
        const prefix = `GT-${productCode(name)}-`;
        const nums = items.map(i => String(i.sku || '').toUpperCase())
            .filter(code => code.startsWith(prefix))
            .map(code => Number(code.slice(prefix.length)))
            .filter(Number.isFinite);
        const next = (nums.length ? Math.max(...nums) : 0) + 1;
        skuInput.value = `${prefix}${String(next).padStart(3, '0')}`;
        skuInput.dataset.suggested = 'true';
    };

    const sortItems = rows => [...rows].sort((a, b) => {
        const ad = new Date(a.lastReceivedAt || a.createdAt || 0).getTime();
        const bd = new Date(b.lastReceivedAt || b.createdAt || 0).getTime();
        return bd - ad || Number(b.id) - Number(a.id);
    });

    const productGroups = rows => {
        const map = new Map();
        rows.forEach(item => {
            const key = String(item.itemName || '').trim().toLowerCase();
            if (!key) return;
            const current = map.get(key) || {
                name: item.itemName,
                total: 0,
                minimum: Number(item.reorderLevel || 0),
                ids: [],
                skus: []
            };
            current.total += Number(item.quantity || 0);
            current.minimum = Math.max(current.minimum, Number(item.reorderLevel || 0));
            if (item.id != null) current.ids.push(Number(item.id));
            if (item.sku) current.skus.push(String(item.sku));
            map.set(key, current);
        });
        return [...map.values()]
            .map(product => ({...product, lowStock: product.minimum > 0 && product.total <= product.minimum}))
            .sort((a, b) => String(a.name || '').localeCompare(String(b.name || '')));
    };

    const productIdentifier = product => {
        const sku = String(product?.skus?.[0] || '').trim().toUpperCase();
        if (sku) return sku.replace(/-\d+$/, '') || sku;
        return `GT-${productCode(product?.name || 'ITEM')}`;
    };

    const renderOverviewCards = products => {
        const totalUnits = products.reduce((sum, product) => sum + Number(product.total || 0), 0);
        const lowStock = products.filter(product => product.lowStock);
        const productRows = products.length ? products.map(product => `
            <div class="inventory-stat-row inventory-product-row">
                <div class="inventory-stat-copy inventory-stat-copy-wide">
                    <b>${GT.escape(product.name)}</b>
                </div>
                <span class="inventory-product-id">${GT.escape(productIdentifier(product))}</span>
            </div>`).join('') : `<div class="inventory-stat-empty"><b>No products yet</b><small>Add the first stock record to create a product.</small></div>`;

        const unitRows = products.length ? products.map(product => `
            <div class="inventory-stat-row inventory-unit-row">
                <div class="inventory-stat-copy inventory-stat-copy-wide">
                    <b>${GT.escape(product.name)}</b>
                </div>
                <span class="inventory-stat-units inventory-unit-block"><strong>${Number(product.total || 0).toLocaleString()}</strong><em>units</em></span>
            </div>`).join('') : `<div class="inventory-stat-empty"><b>No units recorded</b><small>Current inventory is empty.</small></div>`;

        const lowStockRows = (lowStock.length ? lowStock : products).map(product => `
            <div class="inventory-stat-row ${product.lowStock ? 'inventory-stat-row-warning' : 'inventory-stat-row-ok'} inventory-low-stock-row">
                <div class="inventory-stat-copy inventory-stat-copy-wide">
                    <b>${GT.escape(product.name)}</b>
                </div>
                <div class="inventory-stock-metrics">
                    <span class="inventory-minimum">Minimum ${Number(product.minimum || 0).toLocaleString()}</span>
                    <span class="inventory-stat-units inventory-unit-block"><strong>${Number(product.total || 0).toLocaleString()}</strong><em>units</em></span>
                </div>
            </div>`).join('') || `<div class="inventory-stat-empty"><b>No products yet</b><small>Low-stock status will appear after inventory is added.</small></div>`;

        document.getElementById('inventoryStats').innerHTML = `
            <div class="mini-stat inventory-stat-card">
                <div class="inventory-stat-head"><div><span>Products</span><strong>${products.length}</strong></div><span class="inventory-stat-mark">P</span></div>
                <div class="inventory-stat-list">${productRows}</div>
            </div>
            <div class="mini-stat inventory-stat-card">
                <div class="inventory-stat-head"><div><span>Total units</span><strong>${totalUnits.toLocaleString()}</strong></div><span class="inventory-stat-mark">Σ</span></div>
                <div class="inventory-stat-list">${unitRows}</div>
            </div>
            <div class="mini-stat inventory-stat-card">
                <div class="inventory-stat-head"><div><span>Low stock products</span><strong>${lowStock.length}</strong></div><span class="inventory-stat-mark">!</span></div>
                <div class="inventory-stat-list">${lowStockRows}</div>
            </div>`;
    };

    const render = () => {
        const q = search.value.trim().toLowerCase();
        const visible = sortItems(items);
        const rows = visible.filter(i => !q || [i.sku, i.itemName, i.warehouseName, i.vendorName]
            .some(x => String(x || '').toLowerCase().includes(q)));
        const products = productGroups(items);

        document.getElementById('inventoryCount').textContent = `${rows.length} stock record${rows.length === 1 ? '' : 's'}`;
        renderOverviewCards(products);

        table.innerHTML = rows.length ? rows.map(i => {
            const actions = [];
            if (canAdjust) actions.push(`<button class="tiny-button inventory-action-btn" data-adjust-stock="${i.id}">Adjust stock</button>`);
            if (i.reviewPending && canReview && i.vendorStatus !== 'INACTIVE') {
                actions.push(`<button class="tiny-button inventory-action-btn supply-review-btn" data-review-supply="${i.id}">Review partner</button>`);
            } else if (i.vendorName && i.reviewed) {
                actions.push(`<button class="tiny-button inventory-action-btn reviewed-action" type="button" disabled><span aria-hidden="true">✓</span> Reviewed</button>`);
            }
            const depleted = Number(i.quantity || 0) <= 0;
            const statusText = depleted ? 'Out of stock' : (i.lowStock ? 'Low stock' : 'Healthy');
            const statusClass = depleted ? 'status-cancelled' : (i.lowStock ? 'status-pending' : 'status-approved');
            const recordedUnits = i.lastReceivedQuantity != null ? Number(i.lastReceivedQuantity) : Number(i.quantity || 0);
            return `<tr>
                <td><span class="inventory-sku">${GT.escape(i.sku)}</span></td>
                <td><div class="inventory-item-cell"><strong>${GT.escape(i.itemName)}</strong></div></td>
                <td><div class="inventory-secondary-cell"><strong>${GT.escape(i.warehouseName || '—')}</strong><span>GlobalTrade warehouse</span></div></td>
                <td><div class="inventory-secondary-cell"><strong>${GT.escape(i.vendorName || '—')}</strong><span>Supplier</span></div></td>
                <td><div class="inventory-stock-cell"><strong>${recordedUnits.toLocaleString()} <span>units</span></strong><small>Preferred minimum ${Number(i.reorderLevel || 0).toLocaleString()}</small></div></td>
                <td><div class="inventory-received-cell"><strong>${GT.date(i.lastReceivedAt || i.createdAt, true)}</strong><span>${Number(i.lastReceivedQuantity || 0).toLocaleString()} received</span></div></td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td><div class="inventory-row-actions">${actions.join('') || '<span class="muted-action">View only</span>'}</div></td>
            </tr>`;
        }).join('') : GT.empty('No inventory found');

        document.querySelectorAll('[data-adjust-stock]').forEach(button => button.addEventListener('click', () => {
            const item = items.find(row => Number(row.id) === Number(button.dataset.adjustStock));
            if (!item) return;
            const form = document.getElementById('inventoryAdjustForm');
            form.reset();
            form.inventoryItemId.value = String(item.id);
            form.adjustmentType.value = 'ADD';
            form.units.value = '1';
            document.getElementById('inventoryAdjustTitle').textContent = `Adjust ${item.itemName}`;
            document.getElementById('inventoryAdjustContext').innerHTML = `
                <div><span>SKU</span><strong>${GT.escape(item.sku)}</strong></div>
                <div><span>Current stock</span><strong>${Number(item.quantity || 0).toLocaleString()} units</strong></div>`;
            document.getElementById('inventoryAdjustModal').classList.add('open');
        }));

        document.querySelectorAll('[data-review-supply]').forEach(button => button.addEventListener('click', () => {
            const item = items.find(row => Number(row.id) === Number(button.dataset.reviewSupply));
            if (!item) return;
            const form = document.getElementById('supplyReviewForm');
            form.reset();
            form.inventoryItemId.value = String(item.id);
            setRating(5);
            document.getElementById('supplyReviewTitle').textContent = `Review ${item.vendorName || 'partner'}`;
            document.getElementById('supplyReviewContext').innerHTML = `
                <div><span>Item</span><strong>${GT.escape(item.itemName)}</strong></div>
                <div><span>SKU</span><strong>${GT.escape(item.sku)}</strong></div>
                <div><span>Received</span><strong>${Number(item.lastReceivedQuantity || 0).toLocaleString()} units</strong></div>
                <div><span>Received at</span><strong>${GT.date(item.lastReceivedAt, true)}</strong></div>`;
            document.getElementById('supplyReviewModal').classList.add('open');
        }));
    };

    const load = async () => {
        items = await GT.api('/inventory');
        render();
    };

    const loadOptions = async () => {
        if (!document.querySelector('[data-open-modal="inventoryModal"]')) return;
        [warehouses, suppliers] = await Promise.all([GT.api('/inventory/warehouses'), GT.api('/vendors')]);
        suppliers = suppliers.filter(v => v.status === 'ACTIVE');
        warehouseSelect.innerHTML = '<option value="" selected disabled>Select</option>' + warehouses.map(w => `<option value="${w.id}">${GT.escape(w.name)}</option>`).join('');
        supplierSelect.innerHTML = '<option value="" selected disabled>Select</option>' + suppliers.map(v => `<option value="${v.id}">${GT.escape(v.name)}</option>`).join('');
        warehouseSelect.value = '';
        supplierSelect.value = '';
    };

    const resetInventoryForm = () => {
        const form = document.getElementById('inventoryForm');
        form?.reset();
        if (warehouseSelect) warehouseSelect.value = '';
        if (supplierSelect) supplierSelect.value = '';
        if (skuInput) {
            skuInput.value = '';
            delete skuInput.dataset.suggested;
        }
        if (reorderInput) {
            reorderInput.value = '';
            reorderInput.readOnly = false;
            delete reorderInput.dataset.productMinimum;
        }
    };

    document.querySelector('[data-open-modal="inventoryModal"]')?.addEventListener('click', resetInventoryForm);
    document.getElementById('inventoryModal')?.addEventListener('gt:modal-reset', resetInventoryForm);
    itemNameInput?.addEventListener('input', () => {
        if (skuInput?.dataset.suggested === 'true') skuInput.value = '';
        syncProductMinimum();
    });
    itemNameInput?.addEventListener('blur', () => {
        suggestSku();
        syncProductMinimum();
    });
    skuInput?.addEventListener('input', () => {
        delete skuInput.dataset.suggested;
    });
    search.addEventListener('input', render);

    document.getElementById('inventoryForm')?.addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const body = GT.formObject(form);
        ['quantity','reorderLevel','warehouseId','vendorId'].forEach(k => body[k] = Number(body[k]));
        try {
            const created = await GT.api('/inventory', {method:'POST', body:JSON.stringify(body)});
            await load();
            document.getElementById('inventoryModal').classList.remove('open');
            resetInventoryForm();
            GT.toast('Stock record added', `${Number(body.quantity).toLocaleString()} units of ${created.itemName} recorded as ${created.sku}.`);
            GT.signalChange?.('inventory','shipments','dashboard','vendors','alerts');
            await GT.refreshAlertIndicator?.({notify:true});
        } catch (error) {
            GT.toast('Unable to add inventory', error.message, 'error');
        }
    });

    document.getElementById('inventoryAdjustForm')?.addEventListener('submit', async event => {
        event.preventDefault();
        const body = GT.formObject(event.currentTarget);
        const itemId = Number(body.inventoryItemId);
        const units = Number(body.units);
        if (!Number.isInteger(units) || units <= 0) return GT.toast('Unable to update stock', 'Enter a valid whole number of units.', 'error');
        const delta = body.adjustmentType === 'REMOVE' ? -units : units;
        try {
            const updated = await GT.api(`/inventory/${itemId}/adjust`, {method:'POST', body:JSON.stringify({delta})});
            await load();
            document.getElementById('inventoryAdjustModal').classList.remove('open');
            GT.toast('Stock updated', `${updated.sku} now has ${Number(updated.quantity).toLocaleString()} units available.`);
            GT.signalChange?.('inventory','shipments','dashboard','alerts');
            await GT.refreshAlertIndicator?.({notify:true});
        } catch (error) {
            GT.toast('Stock update failed', error.message, 'error');
        }
    });

    document.getElementById('supplyReviewForm')?.addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const body = GT.formObject(form);
        const itemId = Number(body.inventoryItemId);
        const rating = Number(body.rating);
        try {
            await GT.api(`/inventory/${itemId}/partner-review`, {method:'POST', body:JSON.stringify({rating, notes:String(body.notes || '').trim()})});
            document.getElementById('supplyReviewModal').classList.remove('open');
            setRating(5);
            await load();
            GT.toast('Partner review saved', `${formatRating(rating)} / 5 recorded for this supply.`);
            GT.signalChange?.('vendors','inventory','dashboard','alerts');
            await GT.refreshAlertIndicator?.({notify:true});
        } catch (error) {
            GT.toast('Unable to save review', error.message, 'error');
        }
    });

    GT.onExternalChange?.(['inventory','vendors','shipments','customs'], () => Promise.all([load(),loadOptions()]).catch(()=>{}));
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) load().catch(()=>{});
    });

    setupStarPicker();
    Promise.all([load(), loadOptions()]).catch(error => GT.toast('Inventory unavailable', error.message, 'error'));
})();
