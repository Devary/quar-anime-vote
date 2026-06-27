package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "anime")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Anime extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(nullable = false, unique = true, length = 255)
    public String name;

    @Column(length = 2048)
    public String imageUrl;
}
