package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="stock_receipts")
public class StockReceipt {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="inventory_item_id", nullable=false)
    private InventoryItem inventoryItem;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="vendor_id", nullable=false)
    private Vendor vendor;

    @Column(name="quantity_received", nullable=false)
    private int quantityReceived;

    @Column(name="received_at", nullable=false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Column(name="received_by", nullable=false, length=60)
    private String receivedBy = "SYSTEM";

    @Column(name="reviewed", nullable=false)
    private boolean reviewed = false;

    public StockReceipt(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public InventoryItem getInventoryItem(){return inventoryItem;} public void setInventoryItem(InventoryItem v){this.inventoryItem=v;}
    public Vendor getVendor(){return vendor;} public void setVendor(Vendor v){this.vendor=v;}
    public int getQuantityReceived(){return quantityReceived;} public void setQuantityReceived(int v){this.quantityReceived=v;}
    public LocalDateTime getReceivedAt(){return receivedAt;} public void setReceivedAt(LocalDateTime v){this.receivedAt=v;}
    public String getReceivedBy(){return receivedBy;} public void setReceivedBy(String v){this.receivedBy=v;}
    public boolean isReviewed(){return reviewed;} public void setReviewed(boolean v){this.reviewed=v;}
}
