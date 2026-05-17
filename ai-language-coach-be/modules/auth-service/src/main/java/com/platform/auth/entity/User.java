package com.platform.auth.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    public enum UserStatus {
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        SUSPENDED,
        ACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String provider;
    
    @Column(nullable = false)
    private String tenantId;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING_APPROVAL'")
    private UserStatus status = UserStatus.PENDING_APPROVAL;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(nullable = false)
    private boolean accountNonExpired = true;
    
    @Column(nullable = false)
    private boolean accountNonLocked = true;
    
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    private String businessName;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Column(name = "approved_by")
    private UUID approvedBy;
    
    private LocalDateTime approvedAt;
    
    private String approvalComment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(UUID id, String email, String password, String firstName, String lastName, 
                String provider, String tenantId, Set<Role> roles, UserStatus status, boolean enabled, 
                boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired,
                String businessName, UUID createdBy, UUID approvedBy, LocalDateTime approvedAt,
                String approvalComment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.provider = provider;
        this.tenantId = tenantId;
        this.roles = roles;
        this.status = status;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.businessName = businessName;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.approvalComment = approvalComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return email; }
    @Override
    public boolean isAccountNonExpired() { return accountNonExpired; }
    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override
    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    @Override
    public boolean isEnabled() { return enabled; }

    public boolean isApproved() {
        return status == UserStatus.APPROVED || status == UserStatus.ACTIVE;
    }

    public boolean canAccess() {
        Role primaryRole = roles != null && !roles.isEmpty() ? roles.iterator().next() : Role.USER_STUDENT;
        if (primaryRole == Role.USER_STUDENT) return true;
        return isApproved();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getApprovalComment() { return approvalComment; }
    public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class UserBuilder {
        private UUID id;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String provider;
        private String tenantId;
        private Set<Role> roles = java.util.Set.of(Role.USER_STUDENT);
        private UserStatus status = User.UserStatus.PENDING_APPROVAL;
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private String businessName;
        private UUID createdBy;
        private UUID approvedBy;
        private LocalDateTime approvedAt;
        private String approvalComment;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserBuilder provider(String provider) { this.provider = provider; return this; }
        public UserBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public UserBuilder role(Role role) { this.roles = java.util.Set.of(role); return this; }
        public UserBuilder roles(Set<Role> roles) { this.roles = roles; return this; }
        public UserBuilder status(UserStatus status) { this.status = status; return this; }
        public UserBuilder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public UserBuilder businessName(String businessName) { this.businessName = businessName; return this; }
        public UserBuilder createdBy(UUID createdBy) { this.createdBy = createdBy; return this; }

        public User build() {
            return new User(id, email, password, firstName, lastName, provider, tenantId, roles, status, enabled,
                    accountNonExpired, accountNonLocked, credentialsNonExpired, businessName, createdBy, 
                    approvedBy, approvedAt, approvalComment, createdAt, updatedAt);
        }
    }
}