package com.votescroll.dto;
import lombok.*;
import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class MultiPollCreateDto {
    public String anime;
    public String question;
    public List<GroupCreateDto> groups;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GroupCreateDto {
        public String label;
        public List<String> characterIds;
    }
}
