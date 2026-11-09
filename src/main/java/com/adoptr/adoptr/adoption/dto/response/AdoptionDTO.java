package com.adoptr.adoptr.adoption.dto.response;

import com.adoptr.adoptr.publication.dto.response.PhotoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Detalle público de una publicación de adopción. */
@Getter
@Setter
public class AdoptionDTO extends AdoptionSummaryDTO {
    private String description;
    private OwnerDTO owner;
    /** En orden: la primera es la portada. Vacía si no tiene fotos. */
    private List<PhotoDTO> photos;
    /**
     * {@code true} solo si el request trae un JWT válido de quien publicó. Decide qué acciones
     * muestra la interfaz; los permisos reales los verifica el servicio en cada operación.
     */
    private boolean viewerIsOwner;
}
