package com.votescroll.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "multi_poll")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPoll extends PanacheEntityBase {
    @Id
    public String id;
    public String anime;
    public String question;
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("groupOrder ASC")
    public List<MultiPollGroup> groups;
}
