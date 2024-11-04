package com.example.adoptr_backend.service.dto.request;

import com.example.adoptr_backend.model.AdoptionStatusType;
import lombok.Data;

@Data
public class AdoptionStatusDTOin {
    private AdoptionStatusType nextStatus;
    private Long adoptionId;
    private Long contactUserId;

}
