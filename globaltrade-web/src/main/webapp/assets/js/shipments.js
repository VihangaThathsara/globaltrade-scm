(() => {
    let shipments = [];
    let inventory = [];
    let products = [];
    const canWrite = !!document.querySelector('[data-open-modal="shipmentModal"]');
    const table = document.getElementById('shipmentTable');
    const search = document.getElementById('shipmentSearch');
    const filter = document.getElementById('shipmentStatusFilter');
    const productSelect = document.getElementById('shipmentProduct');
    const etaInput = document.querySelector('#shipmentForm input[name="eta"]');

    const allowedStatuses = shipment => {
        const current = shipment.status;
        if (current === 'CANCELLED') return ['CANCELLED'];
        if (current === 'DELIVERED') return ['DELIVERED'];
        if (current === 'CREATED') return ['CREATED','READY','CANCELLED'];
        if (current === 'READY') return ['READY','IN_TRANSIT','CANCELLED'];
        if (current === 'IN_TRANSIT') return ['IN_TRANSIT','DELIVERED','CANCELLED'];
        if (current === 'DELAYED') return ['DELAYED','IN_TRANSIT','DELIVERED','CANCELLED'];
        return [current];
    };

    const statusNotice = shipment => ({
        READY: ['Shipment ready', `${shipment.trackingNumber} is ready for clearance and dispatch.`],
        IN_TRANSIT: ['Shipment dispatched', `${shipment.trackingNumber} has left the facility and is now in transit.`],
        DELIVERED: ['Delivery completed', `${shipment.trackingNumber} was delivered successfully.`],
        CANCELLED: ['Shipment cancelled', `${shipment.trackingNumber} was cancelled. Reserved stock was returned to inventory.`],
        DELAYED: ['Shipment delayed', `${shipment.trackingNumber} is delayed and requires attention.`]
    }[shipment.status] || ['Shipment updated', `${shipment.trackingNumber} was updated successfully.`]);

    const render = () => {
        const q = search.value.trim().toLowerCase();
        const f = filter.value;
        const rows = shipments.filter(s => (!f || s.status === f) && (!q || [s.trackingNumber,s.origin,s.destination,s.carrier,s.itemName,s.warehouseName,s.clearanceStatus,(s.skus || []).join(' ')]
            .some(x => String(x || '').toLowerCase().includes(q))));
        document.getElementById('shipmentCount').textContent = `${rows.length} record${rows.length === 1 ? '' : 's'}`;
        table.innerHTML = rows.length ? rows.map(s => `
            <tr>
                <td><strong>${GT.escape(s.trackingNumber)}</strong><span class="sub-cell">Shipment #${s.id}</span></td>
                <td class="route-cell">${GT.escape(s.origin)} → ${GT.escape(s.destination)}</td>
                <td>${GT.escape(s.carrier)}</td>
                <td>${GT.escape(s.warehouseName || 'GlobalTrade inventory')}</td>
                <td><strong>${GT.escape(s.itemName || '—')}</strong><span class="sub-cell shipment-cargo-meta"><span>${Number(s.quantity || 0).toLocaleString()} units</span></span></td>
                <td>${GT.date(s.eta,true)}</td>
                <td><span class="status-badge ${GT.statusClass(s.clearanceStatus || 'PENDING')}">${GT.pretty(s.clearanceStatus || 'Pending')}</span></td>
                <td><div class="route-score" style="--score:${s.routeScore}%"><i></i><strong>${s.routeScore}</strong></div></td>
                <td><span class="status-badge ${GT.statusClass(s.status)}">${GT.pretty(s.status)}</span></td>
                <td>${canWrite ? `<select class="inline-select status-change" data-id="${s.id}">${allowedStatuses(s).map(v => `<option ${v === s.status ? 'selected' : ''}>${v}</option>`).join('')}</select>` : ''}</td>
            </tr>`).join('') : GT.empty('No shipments match your view');

        document.querySelectorAll('.status-change').forEach(el => el.addEventListener('change', async () => {
            try {
                const updated = await GT.api(`/shipments/${el.dataset.id}/status`, {method:'PUT', body:JSON.stringify({status:el.value})});
                const index = shipments.findIndex(s => Number(s.id) === Number(updated.id));
                if (index >= 0) shipments[index] = updated;
                render();
                const [title,message] = statusNotice(updated);
                GT.toast(title,message,updated.status === 'DELAYED' ? 'warning' : 'success');
                GT.signalChange?.('shipments','inventory','customs','dashboard','alerts');
                await GT.refreshAlertIndicator?.({notify:true});
            } catch (e) {
                GT.toast('Unable to update shipment', e.message, 'error');
                await load();
            }
        }));
    };

    const load = async () => {
        shipments = await GT.api('/shipments');
        render();
    };

    const buildProducts = rows => {
        const grouped = new Map();
        rows.forEach(item => {
            const quantity = Number(item.quantity || 0);
            if (quantity <= 0) return;
            const key = String(item.itemName || '').trim().toLowerCase();
            if (!key) return;
            const current = grouped.get(key) || {name:item.itemName, available:0};
            current.available += quantity;
            grouped.set(key,current);
        });
        return [...grouped.values()].sort((a,b) => String(a.name).localeCompare(String(b.name)));
    };

    const loadInventory = async (preserveSelection = true) => {
        if (!canWrite) return;
        const previousSelection = preserveSelection ? productSelect.value : '';
        inventory = await GT.api('/inventory');
        products = buildProducts(inventory);
        productSelect.innerHTML = '<option value="" selected disabled>Select</option>' + products
            .map(p => `<option value="${GT.escape(p.name)}" data-available="${p.available}">${GT.escape(p.name)} — ${p.available.toLocaleString()} available</option>`).join('');

        if (previousSelection && products.some(p => p.name === previousSelection)) {
            productSelect.value = previousSelection;
        } else {
            productSelect.value = '';
        }
        productSelect.disabled = products.length === 0;
    };

    const localDateTimeValue = date => {
        const pad = v => String(v).padStart(2,'0');
        return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    };

    const resetShipmentForm = () => {
        const form = document.getElementById('shipmentForm');
        form?.reset();
        if (productSelect) productSelect.value = '';
        if (etaInput) {
            etaInput.value = '';
            etaInput.min = localDateTimeValue(new Date(Date.now() + 5*60*1000));
        }
    };

    search.addEventListener('input', render);
    filter.addEventListener('change', render);
    document.querySelector('[data-open-modal="shipmentModal"]')?.addEventListener('click', resetShipmentForm);
    document.getElementById('shipmentModal')?.addEventListener('gt:modal-reset', resetShipmentForm);

    document.getElementById('shipmentForm')?.addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const body = GT.formObject(form);
        body.quantity = Number(body.quantity);
        const selected = products.find(p => p.name === body.productName);
        if (!selected) return GT.toast('Unable to create shipment', 'Select a product.', 'error');
        if (!Number.isInteger(body.quantity) || body.quantity <= 0) return GT.toast('Unable to create shipment', 'Enter a valid shipment quantity.', 'error');
        if (body.quantity > selected.available) {
            return GT.toast('Insufficient stock', `Only ${selected.available.toLocaleString()} units of ${selected.name} are currently available across GlobalTrade inventory.`, 'error');
        }
        const eta = new Date(body.eta);
        if (!body.eta || Number.isNaN(eta.getTime()) || eta.getTime() <= Date.now()) return GT.toast('Unable to create shipment', 'Expected arrival must be in the future.', 'error');
        try {
            const created = await GT.api('/shipments', {method:'POST', body:JSON.stringify(body)});
            shipments = [created, ...shipments.filter(s => Number(s.id) !== Number(created.id))];
            render();
            document.getElementById('shipmentModal').classList.remove('open');
            resetShipmentForm();
            GT.toast('Shipment created', `${created.trackingNumber} reserved ${Number(created.quantity||0).toLocaleString()} units of ${created.itemName}.`);
            GT.signalChange?.('shipments','inventory','customs','dashboard','alerts');
            await GT.refreshAlertIndicator?.({notify:true});
            await loadInventory();
        } catch (err) {
            GT.toast('Unable to create shipment', err.message, 'error');
        }
    });

    GT.onExternalChange?.(['shipments','inventory','customs'], () => Promise.all([load(),loadInventory()]).catch(()=>{}));
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) Promise.all([load(),loadInventory()]).catch(()=>{});
    });

    Promise.all([load(),loadInventory(false)]).then(resetShipmentForm).catch(e => GT.toast('Shipments unavailable', e.message, 'error'));
})();
