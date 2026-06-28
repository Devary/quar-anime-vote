package com.votescroll.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RoleCreateDto {
    public String id;          // e.g. "VIP"
    public String name;        // e.g. "VIP Member"
    public String description;
}
