package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=60)
    private String username;
    @Column(nullable=false, length=120)
    private String operation;
    @Column(name="component_name", nullable=false, length=120)
    private String componentName;
    @Column(nullable=false)
    private boolean success;
    @Column(name="detail_message", length=500)
    private String detailMessage;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt=LocalDateTime.now();
    public AuditLog(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getUsername(){return username;} public void setUsername(String v){this.username=v;}
    public String getOperation(){return operation;} public void setOperation(String v){this.operation=v;}
    public String getComponentName(){return componentName;} public void setComponentName(String v){this.componentName=v;}
    public boolean isSuccess(){return success;} public void setSuccess(boolean v){this.success=v;}
    public String getDetailMessage(){return detailMessage;} public void setDetailMessage(String v){this.detailMessage=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
}
