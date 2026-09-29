package com.globaltrade.scm.entity;

import com.globaltrade.scm.enums.VendorStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="vendor_code", nullable=false, unique=true, length=30)
    private String vendorCode;
    @Column(nullable=false, length=140)
    private String name;
    @Column(nullable=false, length=80)
    private String country;
    @Column(name="contact_email", length=140)
    private String contactEmail;
    @Column(name="performance_score")
    private Double performanceScore;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private VendorStatus status = VendorStatus.ACTIVE;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();
    public Vendor() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getVendorCode(){return vendorCode;} public void setVendorCode(String v){this.vendorCode=v;}
    public String getName(){return name;} public void setName(String v){this.name=v;}
    public String getCountry(){return country;} public void setCountry(String v){this.country=v;}
    public String getContactEmail(){return contactEmail;} public void setContactEmail(String v){this.contactEmail=v;}
    public Double getPerformanceScore(){return performanceScore;} public void setPerformanceScore(Double v){this.performanceScore=v;}
    public VendorStatus getStatus(){return status;} public void setStatus(VendorStatus v){this.status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
}
