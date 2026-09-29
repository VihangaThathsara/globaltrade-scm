(() => {
    let rows = [];
    const list = document.getElementById('auditList');
    const search = document.getElementById('auditSearch');
    const areaLabel = component => String(component || '').replace('Service','').replace('Bean','');
    const actionLabel = (op, component) => {
        const area = areaLabel(component);
        const key = `${area}:${op}`;
        return ({
            'Vendor:create':'Add partner',
            'Vendor:reviewSupply':'Review partner',
            'Vendor:deactivate':'Deactivate partner',
            'Vendor:reactivate':'Reactivate partner',
            'Inventory:create':'Add inventory',
            'Inventory:adjust':'Adjust stock',
            'Shipment:create':'Create shipment',
            'Shipment:updateStatus':'Update shipment status',
            'Shipment:cancel':'Cancel shipment',
            'Customs:updateStatus':'Update clearance status'
        }[key] || GT.pretty(String(op || '').replace(/([A-Z])/g,'_$1')));
    };
    const render = () => {
        const q = search.value.trim().toLowerCase();
        const filtered = rows.filter(x => !q || [x.username,x.operation,x.component,x.detail].some(v => String(v || '').toLowerCase().includes(q)));
        document.getElementById('auditCount').textContent = `${filtered.length} shown`;
        list.innerHTML = filtered.length ? filtered.map(x => `
            <article class="audit-entry ${x.success ? 'audit-success' : 'audit-failed'}">
                <div class="audit-time"><strong>${GT.date(x.createdAt,true)}</strong><span>${GT.escape(x.username)}</span></div>
                <div class="audit-entry-main">
                    <div class="audit-entry-head"><div><span class="audit-area">${GT.escape(areaLabel(x.component))}</span><h4>${GT.escape(actionLabel(x.operation, x.component))}</h4></div><span class="status-badge ${x.success?'status-approved':'status-rejected'}">${x.success?'Success':'Failed'}</span></div>
                    <p>${GT.escape(x.detail || 'Operation completed')}</p>
                </div>
            </article>`).join('') : '<div class="empty-state"><strong>No activity records</strong><span>No operations match the current search.</span></div>';
    };
    async function load(){ rows = await GT.api('/audit?limit=80'); render(); }
    search.addEventListener('input', render);
    document.getElementById('refreshAudit').addEventListener('click', () => load().catch(e => GT.toast('Refresh failed', e.message, 'error')));
    GT.onExternalChange?.(['vendors', 'inventory', 'shipments', 'customs'], () => load().catch(() => {}));
    document.addEventListener('visibilitychange', () => { if (!document.hidden) load().catch(() => {}); });
    load().catch(e => GT.toast('Activity history unavailable', e.message, 'error'));
})();
