package com.adoptr.adoptr.adoption.dto.request;

import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdoptionStatusDTOin {

    @NotNull
    private AdoptionStatus status;
}
