package com.globaltrade.scm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="automation_runs")
public class AutomationRun {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="automation_name", nullable=false, length=120)
    private String automationName;
    @Column(nullable=false, length=20)
    private String status;
    @Column(name="duration_ms", nullable=false)
    private long durationMs;
    @Column(length=500)
    private String message;
    @Column(name="executed_at", nullable=false)
    private LocalDateTime executedAt=LocalDateTime.now();
    public AutomationRun(){}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getAutomationName(){return automationName;} public void setAutomationName(String v){this.automationName=v;}
    public String getStatus(){return status;} public void setStatus(String v){this.status=v;}
    public long getDurationMs(){return durationMs;} public void setDurationMs(long v){this.durationMs=v;}
    public String getMessage(){return message;} public void setMessage(String v){this.message=v;}
    public LocalDateTime getExecutedAt(){return executedAt;} public void setExecutedAt(LocalDateTime v){this.executedAt=v;}
}
