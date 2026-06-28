package com.votescroll.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PollCreateDto {
    public String anime;
    public String question;
    public List<String> fighterIds; // 2-10 character IDs in order
    public boolean isPrivate;
}
