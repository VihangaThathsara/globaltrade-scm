package com.globaltrade.scm.entity;

import com.globaltrade.scm.enums.CustomsStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="customs_documents")
public class CustomsDocument {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="shipment_id")
    private Shipment shipment;
    @Column(nullable=false, length=80)
    private String type;
    @Column(name="reference_no", nullable=false, unique=true, length=60)
    private String referenceNo;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private CustomsStatus status = CustomsStatus.PENDING;
    @Column(nullable=false)
    private LocalDateTime deadline;
    @Column(name="submitted_at")
    private LocalDateTime submittedAt;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();
    public CustomsDocument(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Shipment getShipment(){return shipment;} public void setShipment(Shipment v){this.shipment=v;}
    public String getType(){return type;} public void setType(String v){this.type=v;}
    public String getReferenceNo(){return referenceNo;} public void setReferenceNo(String v){this.referenceNo=v;}
    public CustomsStatus getStatus(){return status;} public void setStatus(CustomsStatus v){this.status=v;}
    public LocalDateTime getDeadline(){return deadline;} public void setDeadline(LocalDateTime v){this.deadline=v;}
    public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime v){this.submittedAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
}
