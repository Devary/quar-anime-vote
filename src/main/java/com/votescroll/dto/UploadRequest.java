package com.votescroll.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UploadRequest {
    public String filename;
    public String mimeType;
    public String data; // base64 encoded
}
