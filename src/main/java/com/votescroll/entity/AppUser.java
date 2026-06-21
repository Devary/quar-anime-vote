package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "app_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser extends PanacheEntityBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    public AppRole role = AppRole.USER;

    @Column(name = "created_at")
    @Builder.Default
    public LocalDateTime createdAt = LocalDateTime.now();

    public enum AppRole { USER, ADMIN }

    public static Optional<AppUser> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public static Optional<AppUser> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
