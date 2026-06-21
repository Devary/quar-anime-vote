package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "poll")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Poll extends PanacheEntityBase {
    @Id
    public String id;
    public String anime;
    public String question;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fighter1_id")
    public AnimeCharacter fighter1;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fighter2_id")
    public AnimeCharacter fighter2;
}
