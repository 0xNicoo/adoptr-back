package com.adoptr.adoptr.chat.model;

import com.adoptr.adoptr.publication.model.Publication;
import com.adoptr.adoptr.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Conversación entre quien publicó y una persona interesada, sobre una publicación en particular:
 * hay como máximo una por publicación y persona interesada. Nace con el primer mensaje.
 * {@code owner} repite el autor de la publicación para filtrar la bandeja sin pasar por la herencia JOINED.
 */
@Entity
@Table(name = "conversation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"publication_id", "interested_user_id"}),
        indexes = {
                @Index(columnList = "owner_user_id, last_message_at"),
                @Index(columnList = "interested_user_id, last_message_at")
        })
@Getter
@Setter
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interested_user_id", nullable = false)
    private User interested;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at", nullable = false)
    private LocalDateTime lastMessageAt;

    /** Último mensaje que vio cada participante; {@code null} si todavía no vio ninguno. */
    @Column(name = "owner_last_read_message_id")
    private Long ownerLastReadMessageId;

    @Column(name = "interested_last_read_message_id")
    private Long interestedLastReadMessageId;

    /** @return el rol de ese usuario en la conversación, o {@code null} si no participa. */
    public ChatRole roleOf(Long userId) {
        if (userId == null) {
            return null;
        }
        if (userId.equals(owner.getId())) {
            return ChatRole.OWNER;
        }
        if (userId.equals(interested.getId())) {
            return ChatRole.INTERESTED;
        }
        return null;
    }

    /** Avanza la marca de lectura de ese rol; nunca la hace retroceder. */
    public void markRead(ChatRole role, Long messageId) {
        if (role == ChatRole.OWNER && isAfter(messageId, ownerLastReadMessageId)) {
            ownerLastReadMessageId = messageId;
        } else if (role == ChatRole.INTERESTED && isAfter(messageId, interestedLastReadMessageId)) {
            interestedLastReadMessageId = messageId;
        }
    }

    private static boolean isAfter(Long messageId, Long current) {
        return messageId != null && (current == null || messageId > current);
    }
}
