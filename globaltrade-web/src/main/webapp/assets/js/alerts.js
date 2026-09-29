(async () => {
    const center = document.getElementById('alertCenter');
    const canResolve = center?.dataset.canResolve === 'true';
    const iconFor = type => ({
        LOW_STOCK:'▦',
        SHIPMENT_DELAY:'↗',
        CUSTOMS_DEADLINE:'▤',
        CUSTOMS_REJECTION:'×',
        VENDOR_PERFORMANCE:'◇',
        SYSTEM:'i'
    }[type] || '!');

    async function load() {
        const alerts = await GT.api('/alerts');
        document.getElementById('alertCount').textContent = `${alerts.length} active`;
        const counts = {CRITICAL:0, WARNING:0, INFO:0};
        alerts.forEach(x => counts[x.severity] = (counts[x.severity] || 0) + 1);

        document.getElementById('alertSummary').innerHTML = `
            <div class="summary-box summary-critical">
                <div class="alert-summary-icon">!</div>
                <div><span>CRITICAL</span><strong>${counts.CRITICAL || 0}</strong><small>Immediate operational action required</small></div>
            </div>
            <div class="summary-box summary-warning">
                <div class="alert-summary-icon">◇</div>
                <div><span>WARNING</span><strong>${counts.WARNING || 0}</strong><small>Operational condition that needs attention</small></div>
            </div>
            <div class="summary-box summary-info">
                <div class="alert-summary-icon">i</div>
                <div><span>INFORMATION</span><strong>${counts.INFO || 0}</strong><small>Informational update from the network</small></div>
            </div>`;

        center.innerHTML = alerts.length ? alerts.map(x => `
            <article class="alert-card alert-${x.severity.toLowerCase()}">
                <div class="alert-card-icon" aria-hidden="true">${iconFor(x.type)}</div>
                <div class="alert-card-body">
                    <div class="alert-card-head">
                        <div>
                            <span class="alert-source">${GT.escape(x.source || GT.pretty(x.type))}</span>
                            <h4>${GT.escape(x.title || GT.pretty(x.type))}</h4>
                        </div>
                    </div>
                    <p>${GT.escape(x.message)}</p>
                    <div class="alert-meta">
                        <span class="alert-severity-chip severity-${x.severity.toLowerCase()}">${GT.escape(x.severity)}</span>
                        <span><b>Reference</b>${GT.escape(x.entityRef || 'System')}</span>
                        <span><b>Raised</b>${GT.date(x.createdAt)}</span>
                    </div>
                </div>
                ${canResolve ? `<button class="tiny-button resolve-btn" data-id="${x.id}">Resolve</button>` : ''}
            </article>`).join('') : '<div class="empty-state alert-empty"><strong>All clear</strong><span>No active operational alerts.</span></div>';

        await GT.refreshAlertIndicator?.({notify:false});
        document.querySelectorAll('.resolve-btn').forEach(button => button.addEventListener('click', async () => {
            try {
                await GT.api(`/alerts/${button.dataset.id}/resolve`, {method:'POST', body:'{}'});
                GT.toast('Alert resolved', 'This operational alert is no longer active.');
                GT.signalChange?.('alerts', 'dashboard');
                await load();
            } catch (error) {
                GT.toast('Unable to resolve alert', error.message, 'error');
            }
        }));
    }

    try { await load(); } catch(error) { GT.toast('Alerts unavailable', error.message, 'error'); }

    GT.onExternalChange?.(['alerts', 'inventory', 'vendors', 'shipments', 'customs'], () => load().catch(() => {}));
    window.addEventListener('gt:alerts-updated', () => load().catch(() => {}));
    window.setInterval(() => load().catch(() => {}), 3000);
})();
