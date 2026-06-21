package com.votescroll.dto;

import com.votescroll.entity.AnimeCharacter;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CharacterDto {
    public String id;
    public String name;
    public String title;
    public String anime;
    public String imageUrl;

    public static CharacterDto from(AnimeCharacter c) {
        return CharacterDto.builder()
            .id(c.id).name(c.name).title(c.title).anime(c.anime).imageUrl(c.imageUrl)
            .build();
    }
}
