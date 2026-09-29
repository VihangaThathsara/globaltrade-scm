package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="performance_metrics")
public class PerformanceMetric {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="component_name", nullable=false, length=120)
    private String componentName;
    @Column(name="operation_name", nullable=false, length=120)
    private String operationName;
    @Column(name="duration_ms", nullable=false)
    private long durationMs;
    @Column(nullable=false)
    private boolean success;
    @Column(name="recorded_at", nullable=false)
    private LocalDateTime recordedAt=LocalDateTime.now();
    public PerformanceMetric(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getComponentName(){return componentName;} public void setComponentName(String v){this.componentName=v;}
    public String getOperationName(){return operationName;} public void setOperationName(String v){this.operationName=v;}
    public long getDurationMs(){return durationMs;} public void setDurationMs(long v){this.durationMs=v;}
    public boolean isSuccess(){return success;} public void setSuccess(boolean v){this.success=v;}
    public LocalDateTime getRecordedAt(){return recordedAt;} public void setRecordedAt(LocalDateTime v){this.recordedAt=v;}
}
