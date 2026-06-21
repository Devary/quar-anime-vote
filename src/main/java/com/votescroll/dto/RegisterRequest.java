package com.votescroll.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public String confirmPassword;
    public String firstName;
    public String lastName;
}
