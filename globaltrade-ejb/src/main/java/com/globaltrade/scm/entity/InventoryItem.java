package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="inventory_items")
public class InventoryItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=40)
    private String sku;

    @Column(name="item_name", nullable=false, length=140)
    private String itemName;

    @Column(nullable=false)
    private int quantity;

    @Column(name="reorder_level", nullable=false)
    private int reorderLevel;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="warehouse_id")
    private Warehouse warehouse;

    @Version
    private long version;

    @Column(nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void touch(){ updatedAt = LocalDateTime.now(); }

    public InventoryItem(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getSku(){return sku;} public void setSku(String v){this.sku=v;}
    public String getItemName(){return itemName;} public void setItemName(String v){this.itemName=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){this.quantity=v;}
    public int getReorderLevel(){return reorderLevel;} public void setReorderLevel(int v){this.reorderLevel=v;}
    public Warehouse getWarehouse(){return warehouse;} public void setWarehouse(Warehouse v){this.warehouse=v;}
    public long getVersion(){return version;}
    public boolean isActive(){return active;} public void setActive(boolean v){this.active=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
}
