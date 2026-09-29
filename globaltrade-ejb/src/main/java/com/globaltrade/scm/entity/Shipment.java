package com.globaltrade.scm.entity;

import com.globaltrade.scm.enums.ShipmentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="shipments")
public class Shipment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="tracking_number", nullable=false, unique=true, length=40)
    private String trackingNumber;

    @Column(nullable=false, length=100)
    private String carrier;

    @Column(nullable=false, length=100)
    private String origin;

    @Column(nullable=false, length=100)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=25)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(nullable=false)
    private LocalDateTime eta;

    @Column(name="route_score", nullable=false)
    private int routeScore = 90;

    @Column(name="created_by", nullable=false, length=60)
    private String createdBy;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private long version;

    @OneToMany(mappedBy="shipment", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<ShipmentItem> items = new ArrayList<>();

    @PreUpdate public void touch(){updatedAt=LocalDateTime.now();}
    public Shipment(){}
    public void addItem(ShipmentItem item){items.add(item); item.setShipment(this);}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getTrackingNumber(){return trackingNumber;} public void setTrackingNumber(String v){this.trackingNumber=v;}
    public String getCarrier(){return carrier;} public void setCarrier(String v){this.carrier=v;}
    public String getOrigin(){return origin;} public void setOrigin(String v){this.origin=v;}
    public String getDestination(){return destination;} public void setDestination(String v){this.destination=v;}
    public ShipmentStatus getStatus(){return status;} public void setStatus(ShipmentStatus v){this.status=v;}
    public LocalDateTime getEta(){return eta;} public void setEta(LocalDateTime v){this.eta=v;}
    public int getRouteScore(){return routeScore;} public void setRouteScore(int v){this.routeScore=v;}
    public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){this.createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
    public long getVersion(){return version;}
    public List<ShipmentItem> getItems(){return items;} public void setItems(List<ShipmentItem> v){this.items=v;}
}
