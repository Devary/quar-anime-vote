package com.votescroll.dto;

import com.votescroll.entity.AppUser;
import com.votescroll.entity.ContentStatus;
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
    public ContentStatus status;
    public boolean isPrivate;
    public String ownerId;
    public String ownerUsername;
    public boolean deletePending;

    public static PollDto from(Poll p) {
        List<CharacterDto> fighters = p.effectiveFighters().stream()
            .map(CharacterDto::from).collect(Collectors.toList());
        String ownerUsername = null;
        if (p.ownerId != null) {
            AppUser owner = AppUser.findById(p.ownerId);
            if (owner != null) ownerUsername = owner.username;
        }
        return PollDto.builder()
            .id(p.id).anime(p.anime).question(p.question)
            .fighters(fighters)
            .status(p.status)
            .isPrivate(p.isPrivate)
            .ownerId(p.ownerId)
            .ownerUsername(ownerUsername)
            .deletePending(p.deletePending)
            .build();
    }
}
