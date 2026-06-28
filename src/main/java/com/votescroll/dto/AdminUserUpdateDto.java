package com.votescroll.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class AdminUserUpdateDto {
    public String email;
    public String profilePicture;
    public List<String> roleIds; // role IDs to assign, e.g. ["USER", "VIP"]
}
