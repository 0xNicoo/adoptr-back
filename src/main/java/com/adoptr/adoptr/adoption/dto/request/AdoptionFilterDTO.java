package com.adoptr.adoptr.adoption.dto.request;

import com.adoptr.adoptr.adoption.model.LifeStage;
import com.adoptr.adoptr.adoption.model.PetSex;
import com.adoptr.adoptr.adoption.model.PetSize;
import com.adoptr.adoptr.adoption.model.Species;
import lombok.Data;

/** Filtros opcionales del catálogo; los que vienen se combinan con Y. */
@Data
public class AdoptionFilterDTO {
    private Species species;
    private PetSex sex;
    private PetSize size;
    private Long provinceId;
    private Long localityId;
    private LifeStage lifeStage;
}
