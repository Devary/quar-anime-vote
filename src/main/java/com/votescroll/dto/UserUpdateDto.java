package com.votescroll.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserUpdateDto {
    public String email;
    public String profilePicture;
}
