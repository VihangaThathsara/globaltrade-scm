package com.globaltrade.scm.entity;

import jakarta.persistence.*;

@Entity
@Table(name="shipment_items")
public class ShipmentItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="shipment_id")
    private Shipment shipment;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="inventory_item_id")
    private InventoryItem inventoryItem;
    @Column(nullable=false)
    private int quantity;
    public ShipmentItem(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Shipment getShipment(){return shipment;} public void setShipment(Shipment v){this.shipment=v;}
    public InventoryItem getInventoryItem(){return inventoryItem;} public void setInventoryItem(InventoryItem v){this.inventoryItem=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){this.quantity=v;}
}
