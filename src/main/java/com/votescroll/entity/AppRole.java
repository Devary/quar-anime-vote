package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_role")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppRole extends PanacheEntityBase {

    /** e.g. "ADMIN", "MODERATOR", "USER", "VIP", "PREMIUM" */
    @Id
    @Column(nullable = false, length = 50)
    public String id;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(length = 500)
    public String description;
}
