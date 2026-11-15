package com.adoptr.adoptr.chat.dto.response;

import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.adoption.model.Species;
import com.adoptr.adoptr.publication.model.PublicationType;

/**
 * La publicación sobre la que se conversa, con lo justo para la bandeja y el encabezado del chat.
 * {@code petName}, {@code species} y {@code status} son de adopción ({@code null} en otros tipos).
 * {@code deleted}: fue dada de baja, así que su detalle ya no se puede abrir.
 */
public record ConversationPublicationDTO(Long id, PublicationType type, String petName, Species species,
                                         AdoptionStatus status, String coverPhotoUrl, boolean deleted) {
}
