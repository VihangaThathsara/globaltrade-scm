package com.globaltrade.scm.entity;

import jakarta.persistence.*;

@Entity
@Table(name="warehouses")
public class Warehouse {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="warehouse_code", nullable=false, unique=true, length=30)
    private String warehouseCode;
    @Column(nullable=false, length=120)
    private String name;
    @Column(nullable=false, length=80)
    private String country;
    public Warehouse(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getWarehouseCode(){return warehouseCode;} public void setWarehouseCode(String v){this.warehouseCode=v;}
    public String getName(){return name;} public void setName(String v){this.name=v;}
    public String getCountry(){return country;} public void setCountry(String v){this.country=v;}
}
