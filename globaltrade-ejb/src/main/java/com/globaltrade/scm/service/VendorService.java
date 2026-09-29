package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.exception.*;
import com.globaltrade.scm.interceptor.*;
import jakarta.annotation.Resource;
import jakarta.annotation.security.*;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.*;

import java.util.List;

@Stateless
@DeclareRoles({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
@Interceptors({PerformanceInterceptor.class, VendorValidationInterceptor.class})
public class VendorService {
    private static final String PARTNER_PREFIX = "GTSC-";

    @PersistenceContext(unitName = "GlobalTradePU") private EntityManager em;
    @Resource private SessionContext sessionContext;
    @EJB private AlertService alertService;

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @Interceptors(AuditInterceptor.class)
    public Vendor create(String code, String name, String country, String email) throws VendorAlreadyExistsException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Company name is required");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country is required");
        String normalizedCode = normalizePartnerCode(code);
        Long existing = em.createQuery("select count(v) from Vendor v where v.vendorCode=:code", Long.class)
                .setParameter("code", normalizedCode).getSingleResult();
        if (existing > 0) throw new VendorAlreadyExistsException("Partner code already exists. Reactivate the existing partner if it was previously deactivated.");

        Vendor vendor = new Vendor();
        vendor.setVendorCode(normalizedCode); vendor.setName(name.trim()); vendor.setCountry(country.trim());
        vendor.setContactEmail(email == null || email.isBlank() ? null : email.trim());
        vendor.setPerformanceScore(null); vendor.setStatus(VendorStatus.ACTIVE);
        em.persist(vendor); em.flush(); return vendor;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Vendor> findAll() { return em.createQuery("select v from Vendor v order by v.id desc", Vendor.class).getResultList(); }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Vendor findRequired(long id) throws VendorNotFoundException {
        Vendor vendor = em.find(Vendor.class, id);
        if (vendor == null) throw new VendorNotFoundException("Partner " + id + " was not found");
        return vendor;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE})
    @Interceptors(AuditInterceptor.class)
    public Vendor reviewSupply(long inventoryItemId, double ratingStars, String notes) throws SupplyChainException {
        InventoryItem item = em.find(InventoryItem.class, inventoryItemId);
        if (item == null) throw new SupplyChainException("Inventory item was not found");

        List<StockReceipt> pending = em.createQuery(
                        "select r from StockReceipt r join fetch r.vendor where r.inventoryItem.id=:inventoryItemId and r.reviewed=false order by r.receivedAt desc, r.id desc",
                        StockReceipt.class)
                .setParameter("inventoryItemId", inventoryItemId).setMaxResults(1).getResultList();
        if (pending.isEmpty()) throw new SupplyChainException("The latest received supply for this inventory item has already been reviewed");

        StockReceipt receipt = pending.get(0);
        Vendor vendor = receipt.getVendor();
        if (vendor == null) throw new VendorNotFoundException("The stock receipt is not linked to a partner");
        if (vendor.getStatus() == VendorStatus.INACTIVE) throw new SupplyChainException("Inactive partners cannot receive new reviews");
        validateRating(ratingStars);

        double reviewScore = ratingStars * 20.0;
        VendorEvaluation evaluation = new VendorEvaluation();
        evaluation.setVendor(vendor);
        evaluation.setInventoryItem(item);
        evaluation.setStockReceipt(receipt);
        evaluation.setScore(reviewScore);
        evaluation.setSupplyQuantity(Math.max(0, receipt.getQuantityReceived()));
        String cleanedNotes = notes == null ? null : notes.trim();
        evaluation.setNotes(cleanedNotes == null || cleanedNotes.isBlank() ? null : cleanedNotes);
        String reviewer = "SYSTEM";
        if (sessionContext != null && sessionContext.getCallerPrincipal() != null) reviewer = sessionContext.getCallerPrincipal().getName();
        evaluation.setReviewedBy(reviewer);
        em.persist(evaluation);
        receipt.setReviewed(true);
        em.flush();

        Double averageScore = em.createQuery("select avg(e.score) from VendorEvaluation e where e.vendor.id=:vendorId", Double.class)
                .setParameter("vendorId", vendor.getId()).getSingleResult();
        double finalScore = averageScore == null ? reviewScore : averageScore;
        vendor.setPerformanceScore(finalScore);
        vendor.setStatus(finalScore < 45.0 ? VendorStatus.SUSPENDED : VendorStatus.ACTIVE);
        syncPerformanceAlert(vendor);
        em.flush();
        return vendor;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public VendorEvaluation reviewForInventory(long inventoryItemId) {
        List<VendorEvaluation> rows = em.createQuery(
                        "select e from VendorEvaluation e where e.inventoryItem.id=:inventoryItemId order by e.evaluatedAt desc, e.id desc",
                        VendorEvaluation.class)
                .setParameter("inventoryItemId", inventoryItemId).setMaxResults(1).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public VendorEvaluation latestReview(long vendorId) {
        List<VendorEvaluation> rows = em.createQuery("select e from VendorEvaluation e where e.vendor.id=:vendorId order by e.evaluatedAt desc, e.id desc", VendorEvaluation.class)
                .setParameter("vendorId", vendorId).setMaxResults(1).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<VendorEvaluation> reviewHistory(long vendorId) throws VendorNotFoundException {
        findRequired(vendorId);
        return em.createQuery("select e from VendorEvaluation e left join fetch e.inventoryItem where e.vendor.id=:vendorId order by e.evaluatedAt desc, e.id desc", VendorEvaluation.class)
                .setParameter("vendorId", vendorId).setMaxResults(30).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long reviewCount(long vendorId) {
        return em.createQuery("select count(e) from VendorEvaluation e where e.vendor.id=:vendorId", Long.class).setParameter("vendorId", vendorId).getSingleResult();
    }

    @RolesAllowed(SecurityRoles.ADMIN)
    @Interceptors(AuditInterceptor.class)
    public Vendor deactivate(long id) throws VendorNotFoundException {
        Vendor vendor = findRequired(id); vendor.setStatus(VendorStatus.INACTIVE);
        alertService.resolveByTypeAndEntity(AlertType.VENDOR_PERFORMANCE, vendor.getVendorCode());
        return vendor;
    }

    @RolesAllowed(SecurityRoles.ADMIN)
    @Interceptors(AuditInterceptor.class)
    public Vendor reactivate(long id) throws VendorNotFoundException {
        Vendor vendor = findRequired(id);
        if (vendor.getStatus() == VendorStatus.INACTIVE) {
            vendor.setStatus(vendor.getPerformanceScore() != null && vendor.getPerformanceScore() < 45.0 ? VendorStatus.SUSPENDED : VendorStatus.ACTIVE);
            syncPerformanceAlert(vendor);
        }
        return vendor;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Vendor> findAtRiskSystem() {
        return em.createQuery("select v from Vendor v where v.performanceScore is not null and v.performanceScore < 70 and v.status <> :inactive order by v.performanceScore asc", Vendor.class)
                .setParameter("inactive", VendorStatus.INACTIVE).getResultList();
    }

    private void syncPerformanceAlert(Vendor vendor) {
        if (vendor.getPerformanceScore() != null && vendor.getPerformanceScore() < 70.0 && vendor.getStatus() != VendorStatus.INACTIVE) {
            double stars = vendor.getPerformanceScore() / 20.0;
            alertService.create(AlertType.VENDOR_PERFORMANCE, AlertSeverity.WARNING,
                    vendor.getName() + " needs attention: average partner rating is " + formatStars(stars) + "/5 (" + Math.round(vendor.getPerformanceScore()) + "%)",
                    vendor.getVendorCode());
        } else {
            alertService.resolveByTypeAndEntity(AlertType.VENDOR_PERFORMANCE, vendor.getVendorCode());
        }
    }

    private String formatStars(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.rint(rounded) ? String.valueOf((int) rounded) : String.valueOf(rounded);
    }

    private String normalizePartnerCode(String code) {
        String value = code == null ? "" : code.trim().toUpperCase();
        if (value.isBlank()) throw new IllegalArgumentException("Partner code is required");
        if (value.startsWith(PARTNER_PREFIX)) value = value.substring(PARTNER_PREFIX.length());
        else if (value.startsWith("VND-")) value = value.substring(4);
        value = value.replaceAll("[^A-Z0-9-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (value.isBlank()) throw new IllegalArgumentException("Enter a valid partner code after GTSC-");
        return PARTNER_PREFIX + value;
    }

    private void validateRating(double ratingStars) throws SupplyChainException {
        if (ratingStars < 1.0 || ratingStars > 5.0) throw new SupplyChainException("Partner rating must be between 1 and 5 stars");
        if (Math.abs(ratingStars - Math.rint(ratingStars)) > 0.00001) throw new SupplyChainException("Partner rating must be a whole number from 1 to 5 stars");
    }
}
