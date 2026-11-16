package com.adoptr.adoptr.profile.dto.request;

import com.adoptr.adoptr.profile.model.ProfileType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProfileDTOin extends ProfileUpdateDTOin {
    @NotNull
    private ProfileType profileType;
}
