package com.votescroll.dto;

import com.votescroll.entity.MultiPollGroup;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollGroupDto {
    public String id;
    public String label;
    public int groupOrder;
    public Instant startDate;
    public Instant endDate;
    public List<CharacterDto> candidates;

    public static MultiPollGroupDto from(MultiPollGroup g) {
        return MultiPollGroupDto.builder()
            .id(g.id).label(g.label).groupOrder(g.groupOrder)
            .startDate(g.startDate).endDate(g.endDate)
            .candidates(g.candidates.stream().map(CharacterDto::from).collect(Collectors.toList()))
            .build();
    }
}
