package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnimeCharacter extends PanacheEntityBase {
    @Id
    public String id;
    public String name;
    public String title;
    public String anime;
    @Column(length = 512)
    public String imageUrl;
}
