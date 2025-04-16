package com.shopsavvy.shopshavvy.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String email;

    private String firstName;

    private String middleName;

    private String lastName;

    private String password;

    private Boolean isDeleted=false;

    private Boolean isActive=false;

    private boolean isExpired=false;

    private boolean isLocked=false;

    @Column(name = "invalid_attempt_count")
    private int invalidAttemptCount=0;

    @LastModifiedDate
    private LocalDateTime passwordUpdateDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private Set<Address> addresses = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    private LocalDateTime dateCreated;

    @LastModifiedDate
    private LocalDateTime lastUpdated;

    @LastModifiedBy
    private String updatedBy;

    private String defaultAddressId;

    public Optional<Address> getDefaultAddress() {
        return addresses.stream()
                .filter(addr -> addr.getId().equals(defaultAddressId))
                .findFirst();
    }

    public void addRole(Role role){
        this.roles.add(role);
    }
}

