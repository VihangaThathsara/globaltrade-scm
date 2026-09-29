(() => {
    const ctx = window.GT_CONTEXT || '';
    const alertSnapshotKey = 'globaltrade.alerts.snapshot';
    let alertPollStarted = false;
    const dataChangeKey = 'globaltrade.data.change';
    const dataChannel = typeof BroadcastChannel !== 'undefined' ? new BroadcastChannel('globaltrade-data') : null;

    const toastIcon = type => ({
        success:'✓', error:'!', warning:'!', critical:'!', info:'i', alert:'!'
    }[type] || 'i');

    window.GT = {
        async api(path, options = {}) {
            const opts = {...options};
            opts.headers = {'Content-Type':'application/json', ...(opts.headers || {})};
            const response = await fetch(ctx + '/api' + path, opts);
            const type = response.headers.get('content-type') || '';
            let body = null;
            if (type.includes('application/json')) body = await response.json();
            else body = await response.text();
            if (!response.ok) {
                const message = body && typeof body === 'object' ? body.error : `Request failed (${response.status})`;
                throw new Error(message || 'Request failed');
            }
            return body;
        },
        escape(value) { return String(value ?? '').replace(/[&<>'"]/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[ch])); },
        statusClass(status) { return 'status-' + String(status || '').toLowerCase().replaceAll('_','-'); },
        pretty(status) { return String(status || '').replaceAll('_',' ').replace(/\b\w/g, c => c.toUpperCase()); },
        date(value, compact=false) {
            if (!value) return '—';
            const d = new Date(value);
            if (Number.isNaN(d.getTime())) return String(value).replace('T',' ').slice(0,16);
            return new Intl.DateTimeFormat('en', compact ? {month:'short',day:'2-digit',hour:'2-digit',minute:'2-digit'} : {year:'numeric',month:'short',day:'2-digit',hour:'2-digit',minute:'2-digit'}).format(d);
        },
        toast(title, message='', type='success', duration=4200) {
            const host = document.getElementById('toastHost'); if (!host) return;
            const node = document.createElement('div'); node.className = `toast ${type}`;
            node.innerHTML = `<span class="toast-icon" aria-hidden="true">${toastIcon(type)}</span><div class="toast-copy"><strong>${this.escape(title)}</strong><p>${this.escape(message)}</p></div><button type="button" class="toast-close" aria-label="Close">×</button>`;
            node.querySelector('.toast-close').addEventListener('click', () => node.remove());
            host.appendChild(node); setTimeout(() => node.remove(), duration);
        },
        empty(text='No records found') { return `<tr><td colspan="12"><div class="empty-state"><strong>${this.escape(text)}</strong><span>Nothing requires attention here right now.</span></div></td></tr>`; },
        formObject(form) { const fd = new FormData(form); const out = {}; fd.forEach((value,key) => out[key] = value); return out; },
        signalChange(...domains) {
            const clean = [...new Set(domains.flat().filter(Boolean).map(String))];
            if (!clean.length) return;
            const payload = {domains: clean, at: Date.now()};
            try { localStorage.setItem(dataChangeKey, JSON.stringify(payload)); } catch (_) {}
            try { dataChannel?.postMessage(payload); } catch (_) {}
        },
        onExternalChange(domains, handler) {
            const wanted = new Set((Array.isArray(domains) ? domains : [domains]).map(String));
            const callback = payload => {
                if (!payload || !Array.isArray(payload.domains)) return;
                if (payload.domains.some(domain => wanted.has(String(domain)) || wanted.has('*'))) handler(payload);
            };
            const storageHandler = event => {
                if (event.key !== dataChangeKey || !event.newValue) return;
                try { callback(JSON.parse(event.newValue)); } catch (_) {}
            };
            window.addEventListener('storage', storageHandler);
            if (dataChannel) dataChannel.addEventListener('message', event => callback(event.data));
            return () => window.removeEventListener('storage', storageHandler);
        },
        alertTone(severity) { return severity === 'CRITICAL' ? 'critical' : severity === 'WARNING' ? 'warning' : 'info'; }
    };

    window.GT.refreshAlertIndicator = async function ({notify = false} = {}) {
        const indicator = document.getElementById('alertIndicator');
        const link = document.getElementById('topbarAlerts');
        if (!indicator || !link) return [];
        try {
            const alerts = await this.api('/alerts');
            const count = Array.isArray(alerts) ? alerts.length : 0;
            indicator.hidden = count === 0;
            indicator.textContent = count > 99 ? '99+' : String(count);
            link.setAttribute('aria-label', count ? `${count} active alerts` : 'No active alerts');
            link.title = count ? `${count} active alert${count === 1 ? '' : 's'}` : 'No active alerts';

            let previousIds = [];
            try { previousIds = JSON.parse(sessionStorage.getItem(alertSnapshotKey) || '[]'); } catch (_) {}
            const currentIds = alerts.map(a => Number(a.id));
            const previous = new Set(previousIds.map(Number));
            const changed = currentIds.length !== previousIds.length || currentIds.some(id => !previous.has(id));
            if (alertPollStarted && notify) {
                const fresh = alerts.filter(a => !previous.has(Number(a.id)));
                const alertTitle = alert => ({
                    LOW_STOCK: 'Low stock detected',
                    SHIPMENT_DELAY: 'Shipment delay detected',
                    CUSTOMS_DEADLINE: 'Customs action required',
                    CUSTOMS_REJECTION: 'Clearance rejected',
                    VENDOR_PERFORMANCE: 'Partner performance needs attention',
                    SYSTEM: 'Operational notice'
                }[alert.type] || (alert.title || 'Operational alert'));
                fresh.slice(0,3).reverse().forEach(alert => {
                    const type = this.alertTone(alert.severity);
                    this.toast(alertTitle(alert), alert.message || 'A new operational alert requires attention.', type, 6500);
                });
                if (fresh.length > 3) this.toast('More alerts received', `${fresh.length - 3} additional alerts were added.`, 'info', 5000);
            }
            if (changed) window.dispatchEvent(new CustomEvent('gt:alerts-updated', {detail:{alerts, count}}));
            try { sessionStorage.setItem(alertSnapshotKey, JSON.stringify(currentIds)); } catch (_) {}
            alertPollStarted = true;
            return alerts;
        } catch (_) {
            indicator.hidden = true;
            link.setAttribute('aria-label', 'Alerts');
            return [];
        }
    };

    const topbarAlerts = document.getElementById('topbarAlerts');
    if (topbarAlerts) {
        window.GT.refreshAlertIndicator({notify:false});
        window.setInterval(() => window.GT.refreshAlertIndicator({notify:true}), 3000);
    }

    const resetModal = modal => {
        if (!modal || !modal.hasAttribute('data-reset-form')) return;
        modal.querySelectorAll('form').forEach(form => form.reset());
        modal.dispatchEvent(new CustomEvent('gt:modal-reset'));
    };
    const closeModal = modal => {
        if (!modal) return;
        modal.classList.remove('open');
        resetModal(modal);
    };
    document.querySelectorAll('[data-open-modal]').forEach(btn => btn.addEventListener('click', () => {
        const modal = document.getElementById(btn.dataset.openModal);
        if (!modal) return;
        resetModal(modal);
        modal.classList.add('open');
    }));
    document.querySelectorAll('[data-close-modal]').forEach(btn => btn.addEventListener('click', () => closeModal(btn.closest('.modal'))));
    const menu = document.getElementById('menuToggle');
    if (menu) menu.addEventListener('click', () => document.getElementById('sidebar')?.classList.toggle('open'));
    const verifySession = async () => {
        try {
            const response = await fetch(ctx + '/session-status', {cache:'no-store', credentials:'same-origin'});
            const data = await response.json();
            if (!data?.authenticated) {
                try { sessionStorage.removeItem(alertSnapshotKey); } catch (_) {}
                window.location.replace(ctx + '/login.jsp');
                return false;
            }
            return true;
        } catch (_) {
            window.location.reload();
            return false;
        }
    };
    window.addEventListener('pagehide', () => document.documentElement.classList.add('gt-session-checking'));
    window.addEventListener('pageshow', async event => {
        const valid = await verifySession();
        if (!valid) return;
        if (event.persisted) {
            window.location.replace(window.location.href);
            return;
        }
        document.documentElement.classList.remove('gt-session-checking');
    });
    window.addEventListener('focus', async () => {
        const valid = await verifySession();
        if (valid) document.documentElement.classList.remove('gt-session-checking');
    });
})();
