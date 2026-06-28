package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_user",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_user_email",    columnNames = "email")
    })
@Getter @Setter @NoArgsConstructor
public class AppUser extends PanacheEntityBase {

    @Id
    @Column(nullable = false, length = 36)
    public String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 50)
    public String username;

    @Column(name = "password_hash", nullable = false, length = 300)
    public String passwordHash;

    @Column(nullable = false, length = 255)
    public String email;

    @Column(name = "profile_picture", length = 500)
    public String profilePicture;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
        joinColumns        = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    public Set<AppRole> roles = new HashSet<>();

    // ── Finders ───────────────────────────────────────────────────────────────

    public static Optional<AppUser> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public static boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public static boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public boolean hasRole(String roleId) {
        return roles.stream().anyMatch(r -> r.id.equalsIgnoreCase(roleId));
    }
}
