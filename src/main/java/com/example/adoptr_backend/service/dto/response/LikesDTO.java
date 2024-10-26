package com.example.adoptr_backend.service.dto.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikesDTO {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime date;
}
