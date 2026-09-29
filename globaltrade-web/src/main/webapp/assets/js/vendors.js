(() => {
    let vendors = [];
    const grid = document.getElementById('partnerGrid');
    const search = document.getElementById('vendorSearch');
    const canManage = grid?.dataset.canManage === 'true';

    const safeScore = value => {
        if (value === null || value === undefined || value === '') return null;
        const parsed = Number(value);
        return Number.isFinite(parsed) ? Math.max(0, Math.min(100, parsed)) : null;
    };

    const sortPartners = rows => rows.sort((a, b) => {
        if (a.status === 'INACTIVE' && b.status !== 'INACTIVE') return 1;
        if (a.status !== 'INACTIVE' && b.status === 'INACTIVE') return -1;
        return String(a.name || '').localeCompare(String(b.name || ''));
    });

    const formatRating = value => Number(value).toFixed(1).replace('.0', '');

    const staticStars = rating => {
        const value = Math.max(0, Math.min(5, Number(rating) || 0));
        return `<span class="static-rating" aria-label="${GT.escape(formatRating(value))} out of 5 stars">${Array.from({length:5}, (_, index) => {
            const fill = Math.max(0, Math.min(1, value - index));
            return `<span class="static-star"><i>★</i><b style="width:${fill * 100}%">★</b></span>`;
        }).join('')}</span>`;
    };

    const render = () => {
        const q = search.value.trim().toLowerCase();
        const rows = vendors.filter(v => !q || [v.vendorCode, v.name, v.country, v.status]
            .some(x => String(x || '').toLowerCase().includes(q)));

        grid.classList.toggle('partner-grid-empty', !rows.length);
        grid.innerHTML = rows.length ? rows.map(v => {
            const score = safeScore(v.performanceScore);
            const reviewCount = Number(v.reviewCount || 0);
            const hasReviews = reviewCount > 0 && score !== null;
            const status = GT.pretty(v.status);
            const statusClass = String(v.status || '').toLowerCase();
            const notes = String(v.latestReviewNotes || '').trim();
            const reviewedAt = v.latestReviewAt ? GT.date(v.latestReviewAt, true) : '';
            const reviewedBy = String(v.latestReviewedBy || '').trim();
            const averageRating = Number(v.averageRating || 0);
            const actions = [];

            if (hasReviews) actions.push(`<button class="tiny-button history-btn" data-id="${GT.escape(v.id)}">History</button>`);
            if (canManage && v.status === 'INACTIVE') actions.push(`<button class="tiny-button partner-reactivate-btn" data-id="${GT.escape(v.id)}">Reactivate</button>`);
            else if (canManage) actions.push(`<button class="tiny-button partner-remove-btn" data-id="${GT.escape(v.id)}">Deactivate</button>`);

            return `
                <article class="partner-card ${v.status === 'INACTIVE' ? 'partner-card-inactive' : ''}" data-partner-id="${GT.escape(v.id)}">
                    <div class="partner-head">
                        <div class="partner-avatar">${GT.escape(String(v.name || '?').slice(0, 1).toUpperCase())}</div>
                        <div class="partner-identity">
                            <h3>${GT.escape(v.name)}</h3>
                            <div class="partner-tags"><span>${GT.escape(v.vendorCode)}</span></div>
                        </div>
                        <span class="partner-status ${statusClass}">${GT.escape(status)}</span>
                    </div>

                    <div class="partner-details">
                        <div class="partner-detail partner-email">
                            <span>Operations email</span>
                            <strong title="${GT.escape(v.contactEmail || '—')}">${GT.escape(v.contactEmail || '—')}</strong>
                        </div>
                        <div class="partner-detail">
                            <span>Country</span>
                            <strong>${GT.escape(v.country || '—')}</strong>
                        </div>
                    </div>

                    <div class="partner-performance-panel ${hasReviews ? 'reviewed' : 'unreviewed'}">
                        ${hasReviews ? `
                            <div class="partner-performance-head">
                                <div>
                                    <span class="partner-review-label">Performance</span>
                                    <strong class="partner-performance-value">${Math.round(score)}%</strong>
                                </div>
                                <div class="partner-rating-block">
                                    ${staticStars(averageRating)}
                                    <div class="partner-rating-meta"><span>${GT.escape(formatRating(averageRating))} / 5</span><span>${reviewCount} review${reviewCount === 1 ? '' : 's'}</span></div>
                                </div>
                            </div>
                            <div class="partner-score-track partner-score-track-wide"><i style="width:${score}%"></i></div>
                            <div class="partner-review-note">
                                <div class="partner-review-note-head"><span>Latest partner review</span>${reviewedAt ? `<time>${GT.escape(reviewedAt)}${reviewedBy ? `  ${GT.escape(reviewedBy)}` : ''}</time>` : ''}</div>
                                <p>${GT.escape(notes || 'No note provided')}</p>
                            </div>
                        ` : `
                            <div class="partner-no-reviews">
                                <span class="partner-review-label">Performance</span>
                                <strong>No reviews yet</strong>
                            </div>
                        `}
                        <div class="partner-card-actions ${actions.length ? '' : 'partner-actions-spacer'}">${actions.join('')}</div>
                    </div>
                </article>`;
        }).join('') : '<div class="empty-state partner-empty"><strong>No partners found</strong><span>Add a partner or try another search.</span></div>';

        bindCardActions();
    };

    const bindCardActions = () => {
        document.querySelectorAll('.history-btn').forEach(button => button.addEventListener('click', async () => {
            const vendorId = Number(button.dataset.id);
            const vendor = vendors.find(v => Number(v.id) === vendorId);
            if (!vendor) return;

            const list = document.getElementById('reviewHistoryList');
            document.getElementById('reviewHistoryTitle').textContent = `${vendor.name} review history`;
            list.innerHTML = '<div class="empty-state"><strong>Loading reviews…</strong></div>';
            document.getElementById('reviewHistoryModal').classList.add('open');

            try {
                const reviews = await GT.api(`/vendors/${vendorId}/reviews`);
                list.innerHTML = reviews.length ? reviews.map(review => {
                    const rating = Number(review.rating ?? (Number(review.score) / 20));
                    const context = [];
                    if (review.itemName) context.push(`<span>${GT.escape(review.itemName)}</span>`);
                    if (review.sku) context.push(`<span>${GT.escape(review.sku)}</span>`);
                    if (Number(review.supplyQuantity || 0) > 0) context.push(`<span>${Number(review.supplyQuantity).toLocaleString()} units received</span>`);
                    if (review.reviewedBy) context.push(`<span>Reviewed by ${GT.escape(review.reviewedBy)}</span>`);
                    return `
                        <div class="review-history-row">
                            <div class="review-history-rating">
                                ${staticStars(rating)}
                                <strong>${GT.escape(formatRating(rating))} / 5</strong>
                            </div>
                            <div class="review-history-copy">
                                <p>${GT.escape(review.notes || 'No note provided')}</p>
                                <div class="review-history-meta">${context.join('')}<time>${GT.date(review.evaluatedAt)}</time></div>
                            </div>
                        </div>`;
                }).join('') : '<div class="empty-state"><strong>No reviews yet</strong><span>No partner reviews have been recorded yet.</span></div>';
            } catch (error) {
                list.innerHTML = `<div class="empty-state"><strong>Unable to load reviews</strong><span>${GT.escape(error.message)}</span></div>`;
            }
        }));

        document.querySelectorAll('.partner-remove-btn').forEach(button => button.addEventListener('click', () => {
            const vendorId = Number(button.dataset.id);
            const vendor = vendors.find(v => Number(v.id) === vendorId);
            if (!vendor) return;
            document.getElementById('deactivatePartnerId').value = String(vendorId);
            document.querySelector('#deactivatePartnerModal h3').textContent = `Deactivate ${vendor.name}?`;
            document.getElementById('deactivatePartnerModal').classList.add('open');
        }));

        document.querySelectorAll('.partner-reactivate-btn').forEach(button => button.addEventListener('click', async () => {
            const id = Number(button.dataset.id);
            const vendor = vendors.find(v => Number(v.id) === id);
            if (!vendor) return;
            try {
                const updated = await GT.api(`/vendors/${id}/reactivate`, {method: 'POST'});
                const index = vendors.findIndex(v => Number(v.id) === id);
                if (index >= 0) vendors[index] = updated;
                render();
                GT.toast('Partner reactivated', `${vendor.name} is available for new inventory and shipments`);
                GT.signalChange?.('vendors', 'inventory', 'shipments', 'dashboard', 'alerts');
                await GT.refreshAlertIndicator?.({notify:true});
            } catch (error) {
                GT.toast('Unable to reactivate partner', error.message, 'error');
            }
        }));
    };

    const load = async () => {
        vendors = sortPartners(await GT.api('/vendors'));
        render();
    };

    search.addEventListener('input', render);

    document.getElementById('vendorForm')?.addEventListener('submit', async event => {
        event.preventDefault();
        const form = event.currentTarget;
        const body = GT.formObject(form);
        let code = String(body.vendorCode || '').trim().toUpperCase();
        if (!code.startsWith('GTSC-')) {
            code = code.replace(/^VND-/, '');
            code = `GTSC-${code}`;
        }
        body.vendorCode = code;

        try {
            const created = await GT.api('/vendors', {method: 'POST', body: JSON.stringify(body)});
            if (!created || !Number.isFinite(Number(created.id)) || Number(created.id) <= 0) await load();
            else {
                vendors = sortPartners([created, ...vendors.filter(v => Number(v.id) !== Number(created.id))]);
                render();
            }
            document.getElementById('vendorModal').classList.remove('open');
            form.reset();
            GT.toast('Partner added', `${created.vendorCode || code} is ready for supply planning`);
            GT.signalChange?.('vendors', 'inventory', 'shipments', 'dashboard');
        } catch (err) {
            GT.toast('Unable to add partner', err.message, 'error');
        }
    });

    document.getElementById('confirmDeactivatePartner')?.addEventListener('click', async () => {
        const id = Number(document.getElementById('deactivatePartnerId').value);
        if (!Number.isFinite(id) || id <= 0) return;
        const vendor = vendors.find(v => Number(v.id) === id);
        try {
            const updated = await GT.api(`/vendors/${id}`, {method: 'DELETE'});
            const index = vendors.findIndex(v => Number(v.id) === id);
            if (index >= 0) vendors[index] = updated;
            render();
            document.getElementById('deactivatePartnerModal').classList.remove('open');
            document.getElementById('deactivatePartnerId').value = '';
            GT.toast('Partner deactivated', `${vendor?.name || 'Partner'} can be reactivated later`);
            GT.signalChange?.('vendors', 'inventory', 'shipments', 'dashboard', 'alerts');
            await GT.refreshAlertIndicator?.({notify:true});
        } catch (error) {
            GT.toast('Unable to deactivate partner', error.message, 'error');
        }
    });

    GT.onExternalChange?.(['vendors', 'inventory'], () => load().catch(() => {}));
    document.addEventListener('visibilitychange', () => { if (!document.hidden) load().catch(() => {}); });

    load().catch(error => GT.toast('Partners unavailable', error.message, 'error'));
})();
