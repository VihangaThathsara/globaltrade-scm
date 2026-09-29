(() => {
    let docs = [];
    const canWrite = !!document.getElementById('reviewDeadlines');
    const table = document.getElementById('customsTable');
    const allowed = doc => {
        if (['APPROVED','REJECTED','CANCELLED'].includes(doc.status)) return [doc.status];
        if (doc.status === 'PENDING') return ['PENDING','SUBMITTED','REJECTED'];
        if (doc.status === 'SUBMITTED') return ['SUBMITTED','APPROVED','REJECTED'];
        return [doc.status];
    };

    const clearanceNotice = doc => {
        const reference = doc?.referenceNo || 'Clearance document';
        const tracking = doc?.trackingNumber ? ` for ${doc.trackingNumber}` : '';
        return ({
            SUBMITTED: ['Clearance submitted', `${reference}${tracking} was submitted for customs review.`],
            APPROVED: ['Clearance approved', `${reference}${tracking} was approved. The shipment can now be dispatched.`],
            REJECTED: ['Clearance rejected', `${reference}${tracking} was rejected. The shipment was cancelled and reserved stock was returned.`],
            CANCELLED: ['Clearance cancelled', `${reference}${tracking} is no longer active.`],
            PENDING: ['Clearance pending', `${reference}${tracking} is waiting for submission.`]
        }[doc?.status] || ['Clearance updated', `${reference}${tracking} was updated successfully.`]);
    };
    const render = () => {
        document.getElementById('customsCount').textContent = `${docs.length} documents`;
        table.innerHTML = docs.length ? docs.map(c => `<tr>
            <td><strong>${GT.escape(c.referenceNo)}</strong></td>
            <td>${GT.escape(c.trackingNumber)}</td>
            <td class="route-cell">${GT.escape(c.route)}</td>
            <td>${GT.escape(c.type)}</td>
            <td>${GT.date(c.deadline,true)}</td>
            <td><span class="status-badge ${GT.statusClass(c.status)}">${GT.pretty(c.status)}</span></td>
            <td>${canWrite ? `<select class="inline-select customs-status" data-id="${c.id}" ${['APPROVED','REJECTED','CANCELLED'].includes(c.status)?'disabled':''}>${allowed(c).map(v=>`<option ${v===c.status?'selected':''}>${v}</option>`).join('')}</select>` : ''}</td>
        </tr>`).join('') : GT.empty('No clearance documents');
        document.querySelectorAll('.customs-status').forEach(el => el.addEventListener('change', async () => {
            try {
                const updated = await GT.api(`/customs/${el.dataset.id}/status`, {method:'PUT', body:JSON.stringify({status:el.value})});
                const index = docs.findIndex(d => Number(d.id) === Number(updated.id)); if (index >= 0) docs[index] = updated;
                render();
                const [title, message] = clearanceNotice(updated);
                GT.toast(title, message, updated.status === 'REJECTED' ? 'warning' : 'success');
                GT.signalChange?.('customs', 'shipments', 'inventory', 'dashboard', 'alerts');
                await GT.refreshAlertIndicator?.({notify:true});
            } catch (e) { GT.toast('Update failed', e.message, 'error'); await load(); }
        }));
    };
    const load = async () => { docs = await GT.api('/customs'); render(); };
    document.getElementById('reviewDeadlines')?.addEventListener('click', async () => {
        try {
            const r = await GT.api('/customs/review',{method:'POST',body:'{}'});
            GT.toast('Deadline review complete', `${r.overdue} clearance item${r.overdue===1?'':'s'} require attention`);
            GT.signalChange?.('alerts', 'customs', 'dashboard');
            await GT.refreshAlertIndicator?.({notify:true});
        } catch(e){ GT.toast('Review failed',e.message,'error'); }
    });

    GT.onExternalChange?.(['customs', 'shipments', 'inventory'], () => load().catch(() => {}));
    document.addEventListener('visibilitychange', () => { if (!document.hidden) load().catch(() => {}); });
    load().catch(e => GT.toast('Clearance data unavailable', e.message, 'error'));
})();
