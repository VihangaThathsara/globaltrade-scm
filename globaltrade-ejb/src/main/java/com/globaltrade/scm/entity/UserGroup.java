package com.globaltrade.scm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_groups", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "group_name"}))
public class UserGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 60)
    private String username;
    @Column(name = "group_name", nullable = false, length = 60)
    private String groupName;
    public UserGroup() {}
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}
