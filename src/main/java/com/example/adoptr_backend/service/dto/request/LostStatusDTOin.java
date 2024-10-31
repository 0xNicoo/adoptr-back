package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.LostStatusType;
import lombok.Data;

@Data
public class LostStatusDTOin {
    private LostStatusType nextStatus;
    private Long lostId;
    private Long contactUserId;
}
