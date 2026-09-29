package com.globaltrade.scm.entity;

import com.globaltrade.scm.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="alerts")
public class Alert {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private AlertType type;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=15)
    private AlertSeverity severity;
    @Column(nullable=false, length=500)
    private String message;
    @Column(name="entity_ref", length=80)
    private String entityRef;
    @Column(nullable=false)
    private boolean resolved=false;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt=LocalDateTime.now();
    @Column(name="resolved_at")
    private LocalDateTime resolvedAt;
    public Alert(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public AlertType getType(){return type;} public void setType(AlertType v){this.type=v;}
    public AlertSeverity getSeverity(){return severity;} public void setSeverity(AlertSeverity v){this.severity=v;}
    public String getMessage(){return message;} public void setMessage(String v){this.message=v;}
    public String getEntityRef(){return entityRef;} public void setEntityRef(String v){this.entityRef=v;}
    public boolean isResolved(){return resolved;} public void setResolved(boolean v){this.resolved=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getResolvedAt(){return resolvedAt;} public void setResolvedAt(LocalDateTime v){this.resolvedAt=v;}
}
