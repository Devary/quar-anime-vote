package com.votescroll.dto;

import com.votescroll.entity.Poll;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PollDto {
    public String id;
    public String anime;
    public String question;
    public CharacterDto fighter1;
    public CharacterDto fighter2;

    public static PollDto from(Poll p) {
        return PollDto.builder()
            .id(p.id).anime(p.anime).question(p.question)
            .fighter1(CharacterDto.from(p.fighter1))
            .fighter2(CharacterDto.from(p.fighter2))
            .build();
    }
}
