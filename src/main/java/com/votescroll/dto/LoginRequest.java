package com.votescroll.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    public String username;
    public String password;
}
