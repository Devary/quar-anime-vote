package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CharacterCreateDto {
    public String name;
    public String title;
    public String anime;
    public String imageUrl;
}
