package com.adoptr.adoptr.adoption.dto.response;

import lombok.Getter;
import lombok.Setter;

/** Detalle público de una publicación de adopción. */
@Getter
@Setter
public class AdoptionDTO extends AdoptionSummaryDTO {
    private String description;
    private OwnerDTO owner;
    /**
     * {@code true} solo si el request trae un JWT válido de quien publicó. Decide qué acciones
     * muestra la interfaz; los permisos reales los verifica el servicio en cada operación.
     */
    private boolean viewerIsOwner;
}
