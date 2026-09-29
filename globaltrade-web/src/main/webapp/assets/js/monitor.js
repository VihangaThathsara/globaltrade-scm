(() => {
    async function loadMonitor() {
        const d = await GT.api('/monitor');
        const auto = d.automations || [], perf = d.performance || [];

        const latest = new Map();
        auto.forEach(x => { if (!latest.has(x.name)) latest.set(x.name, x); });
        const routines = [...latest.values()];
        const healthy = routines.filter(x => x.status === 'HEALTHY').length;
        const score = routines.length ? Math.round(healthy / routines.length * 100) : null;
        document.getElementById('healthScore').textContent = score === null ? '—' : score + '%';

        document.getElementById('automationList').innerHTML = routines.length
            ? routines.map(x => `<div class="automation-row"><span class="automation-state ${x.status === 'HEALTHY' ? '' : 'degraded'}"></span><div><strong>${GT.escape(x.name)}</strong><span>${GT.escape(x.message || 'No message')}</span><time>${GT.date(x.executedAt,true)}</time></div><em>${x.durationMs} ms</em></div>`).join('')
            : '<div class="empty-state"><strong>Waiting for first cycle</strong><span>Operational checks will appear automatically.</span></div>';

        const perfLatest = new Map();
        perf.forEach(x => {
            const key = `${x.component}|${x.operation}`;
            if (!perfLatest.has(key)) perfLatest.set(key, x);
        });
        const observations = [...perfLatest.values()].slice(0, 10);
        const labels = {
            findAll:'Load records',
            findWarehouses:'Load warehouses',
            findRequired:'Load record',
            create:'Create record',
            updateStatus:'Update status',
            adjust:'Adjust stock',
            reviewSupply:'Review partner',
            cancel:'Cancel shipment',
            stockGuard:'Stock guard',
            shipmentWatch:'Shipment watch',
            partnerPulse:'Partner pulse',
            routeHealthRefresh:'Route health'
        };
        const max = Math.max(1, ...observations.map(x => x.durationMs));
        document.getElementById('metricList').innerHTML = observations.length
            ? observations.map(x => `<div class="metric-row"><div class="metric-name"><strong>${GT.escape(labels[x.operation] || GT.pretty(x.operation.replace(/([A-Z])/g,'_$1')))}</strong><span>${GT.escape(x.component.replace('Service','').replace('Bean',''))}</span></div><div class="metric-bar"><i style="width:${Math.max(4,(x.durationMs/max)*100)}%"></i></div><em>${x.durationMs} ms</em></div>`).join('')
            : '<div class="empty-state"><strong>No observations yet</strong><span>Use the portal and refresh this page.</span></div>';
    }

    GT.onExternalChange?.('*', () => loadMonitor().catch(() => {}));
    document.addEventListener('visibilitychange', () => { if (!document.hidden) loadMonitor().catch(() => {}); });
    window.setInterval(() => loadMonitor().catch(() => {}), 10000);
    loadMonitor().catch(e => GT.toast('Operations health unavailable', e.message, 'error'));
})();
