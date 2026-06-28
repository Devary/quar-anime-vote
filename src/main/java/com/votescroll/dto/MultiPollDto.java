package com.votescroll.dto;

import com.votescroll.entity.AppUser;
import com.votescroll.entity.ContentStatus;
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
    public ContentStatus status;
    public boolean isPrivate;
    public String ownerId;
    public String ownerUsername;
    public boolean deletePending;

    public static MultiPollDto from(MultiPoll mp) {
        String ownerUsername = null;
        if (mp.ownerId != null) {
            AppUser owner = AppUser.findById(mp.ownerId);
            if (owner != null) ownerUsername = owner.username;
        }
        return MultiPollDto.builder()
            .id(mp.id).anime(mp.anime).question(mp.question)
            .groups(mp.groups.stream().map(MultiPollGroupDto::from).collect(Collectors.toList()))
            .status(mp.status)
            .isPrivate(mp.isPrivate)
            .ownerId(mp.ownerId)
            .ownerUsername(ownerUsername)
            .deletePending(mp.deletePending)
            .build();
    }
}
