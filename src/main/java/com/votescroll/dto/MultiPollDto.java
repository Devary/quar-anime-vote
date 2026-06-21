package com.votescroll.dto;

import com.votescroll.entity.MultiPoll;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MultiPollDto {
    public String id;
    public String anime;
    public String question;
    public List<MultiPollGroupDto> groups;

    public static MultiPollDto from(MultiPoll mp) {
        return MultiPollDto.builder()
            .id(mp.id).anime(mp.anime).question(mp.question)
            .groups(mp.groups.stream().map(MultiPollGroupDto::from).collect(Collectors.toList()))
            .build();
    }
}
