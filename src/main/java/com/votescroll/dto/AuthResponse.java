package com.votescroll.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    public String token;
    public Long userId;
    public String username;
    public String role;
}
