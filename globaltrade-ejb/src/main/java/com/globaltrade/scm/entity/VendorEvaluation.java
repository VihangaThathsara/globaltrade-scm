package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="vendor_evaluations",
        uniqueConstraints=@UniqueConstraint(name="uq_vendor_evaluation_receipt", columnNames="stock_receipt_id"))
public class VendorEvaluation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="vendor_id", nullable=false)
    private Vendor vendor;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="inventory_item_id")
    private InventoryItem inventoryItem;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stock_receipt_id")
    private StockReceipt stockReceipt;

    @Column(name="supply_quantity", nullable=false)
    private int supplyQuantity;

    @Column(nullable=false)
    private double score;

    @Column(length=500)
    private String notes;

    @Column(name="reviewed_by", nullable=false, length=60)
    private String reviewedBy = "SYSTEM";

    @Column(name="evaluated_at", nullable=false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public VendorEvaluation(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Vendor getVendor(){return vendor;} public void setVendor(Vendor vendor){this.vendor=vendor;}
    public InventoryItem getInventoryItem(){return inventoryItem;} public void setInventoryItem(InventoryItem inventoryItem){this.inventoryItem=inventoryItem;}
    public StockReceipt getStockReceipt(){return stockReceipt;} public void setStockReceipt(StockReceipt v){this.stockReceipt=v;}
    public int getSupplyQuantity(){return supplyQuantity;} public void setSupplyQuantity(int supplyQuantity){this.supplyQuantity=supplyQuantity;}
    public double getScore(){return score;} public void setScore(double score){this.score=score;}
    public String getNotes(){return notes;} public void setNotes(String notes){this.notes=notes;}
    public String getReviewedBy(){return reviewedBy;} public void setReviewedBy(String reviewedBy){this.reviewedBy=reviewedBy;}
    public LocalDateTime getEvaluatedAt(){return evaluatedAt;} public void setEvaluatedAt(LocalDateTime evaluatedAt){this.evaluatedAt=evaluatedAt;}
}
