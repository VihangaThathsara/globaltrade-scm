(() => {
    async function loadDashboard() {
        const d = await GT.api('/dashboard');
        const stats = [
            ['Active shipments', d.activeShipments, '↗', 'Current movements'],
            ['Delayed', d.delayedShipments, '!', 'Past expected arrival'],
            ['Low stock', d.lowStock, '▦', 'Below minimum level'],
            ['Active partners', d.activeVendors, '◇', 'Connected suppliers'],
            ['Clearance pending', d.pendingCustoms, '▤', 'Documents in progress']
        ];
        document.getElementById('statsGrid').innerHTML = stats.map(s => `<div class="stat-card"><span class="stat-icon">${s[2]}</span><span class="stat-label">${s[0]}</span><strong class="stat-value">${s[1]}</strong><span class="stat-sub">${s[3]}</span></div>`).join('');

        const status = d.shipmentStatuses || {};
        const max = Math.max(1, ...Object.values(status));
        document.getElementById('statusBars').innerHTML = Object.entries(status).map(([k,v]) =>
            `<div class="status-bar-row"><label>${GT.pretty(k)}</label><div class="status-track"><div class="status-fill" style="width:${Math.max(3,(v/max)*100)}%"></div></div><strong>${v}</strong></div>`
        ).join('');

        document.getElementById('dashboardAlerts').innerHTML = d.alerts?.length
            ? d.alerts.map(a => `<div class="alert-mini alert-mini-${a.severity.toLowerCase()}"><div><span class="alert-mini-source">${GT.escape(a.source || GT.pretty(a.type))}</span><strong>${GT.escape(a.message)}</strong><span>${GT.date(a.createdAt,true)}</span></div></div>`).join('')
            : '<div class="empty-state"><strong>All clear</strong><span>No open alerts.</span></div>';

        document.getElementById('recentShipments').innerHTML = d.recentShipments?.length
            ? d.recentShipments.map(s => `<tr><td><strong>${GT.escape(s.trackingNumber)}</strong></td><td class="route-cell">${GT.escape(s.origin)} → ${GT.escape(s.destination)}</td><td>${GT.escape(s.carrier)}</td><td>${GT.escape(s.warehouseName || '—')}</td><td>${GT.date(s.eta,true)}</td><td><span class="status-badge ${GT.statusClass(s.status)}">${GT.pretty(s.status)}</span></td></tr>`).join('')
            : GT.empty('No shipments yet');
    }

    const refreshQuietly = () => loadDashboard().catch(() => {});
    GT.onExternalChange?.(['dashboard', 'inventory', 'vendors', 'shipments', 'customs', 'alerts'], refreshQuietly);
    window.addEventListener('gt:alerts-updated', refreshQuietly);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) refreshQuietly(); });

    loadDashboard().catch(e => GT.toast('Dashboard unavailable', e.message, 'error'));
})();
