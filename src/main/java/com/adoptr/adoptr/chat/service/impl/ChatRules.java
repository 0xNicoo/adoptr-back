package com.adoptr.adoptr.chat.service.impl;

import com.adoptr.adoptr.adoption.model.AdoptionPublication;
import com.adoptr.adoptr.adoption.model.AdoptionStatus;
import com.adoptr.adoptr.publication.model.Publication;
import org.hibernate.Hibernate;

/** Reglas del chat que dependen del estado de la publicación. */
final class ChatRules {

    private ChatRules() {
    }

    /**
     * Una conversación es de solo lectura si su publicación fue dada de baja o, en adopción, ya fue adoptada.
     * No se guarda: si quien publicó vuelve a ponerla en adopción, el chat se reabre.
     */
    static boolean isReadOnly(Publication publication) {
        Object real = Hibernate.unproxy(publication);
        if (publication.getDeletedAt() != null) {
            return true;
        }
        return real instanceof AdoptionPublication adoption && adoption.getStatus() == AdoptionStatus.ADOPTED;
    }
}
