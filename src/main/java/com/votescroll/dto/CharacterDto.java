package com.votescroll.dto;

import com.votescroll.entity.AnimeCharacter;
import com.votescroll.entity.AppUser;
import com.votescroll.entity.ContentStatus;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CharacterDto {
    public String id;
    public String name;
    public String title;
    public String anime;
    public String imageUrl;
    public ContentStatus status;
    public String ownerId;
    public String ownerUsername;

    public static CharacterDto from(AnimeCharacter c) {
        String ownerUsername = null;
        if (c.ownerId != null) {
            AppUser owner = AppUser.findById(c.ownerId);
            if (owner != null) ownerUsername = owner.username;
        }
        return CharacterDto.builder()
            .id(c.id).name(c.name).title(c.title).anime(c.anime).imageUrl(c.imageUrl)
            .status(c.status)
            .ownerId(c.ownerId)
            .ownerUsername(ownerUsername)
            .build();
    }
}
