package com.votescroll.dto;

import com.votescroll.entity.Poll;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PollDto {
    public String id;
    public String anime;
    public String question;
    public List<CharacterDto> fighters;

    public static PollDto from(Poll p) {
        List<CharacterDto> fighters = p.effectiveFighters().stream()
            .map(CharacterDto::from).collect(Collectors.toList());
        return PollDto.builder()
            .id(p.id).anime(p.anime).question(p.question)
            .fighters(fighters)
            .build();
    }
}
