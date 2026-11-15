package com.adoptr.adoptr.chat.repository;

import com.adoptr.adoptr.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Los mensajes más recientes (el orden lo da el {@code Pageable}). */
    Slice<ChatMessage> findByConversationId(Long conversationId, Pageable pageable);

    /** Los mensajes anteriores a {@code before} (cursor), para ir cargando el historial hacia atrás. */
    Slice<ChatMessage> findByConversationIdAndIdLessThan(Long conversationId, Long before, Pageable pageable);

    /** El último mensaje de cada conversación de la lista, en una sola consulta. */
    @Query("""
            select m from ChatMessage m
            where m.id in (select max(x.id) from ChatMessage x
                           where x.conversation.id in :conversationIds
                           group by x.conversation.id)
            """)
    List<ChatMessage> findLastMessages(@Param("conversationIds") Collection<Long> conversationIds);

    /**
     * Mensajes de la otra persona posteriores a la marca de lectura de {@code userId}, por conversación.
     * Las conversaciones sin mensajes sin leer no aparecen.
     */
    @Query("""
            select new com.adoptr.adoptr.chat.repository.UnreadCount(c.id, count(m))
            from ChatMessage m join m.conversation c
            where c.id in :conversationIds
              and m.sender.id <> :userId
              and m.id > coalesce(case when c.owner.id = :userId then c.ownerLastReadMessageId
                                       else c.interestedLastReadMessageId end, 0)
            group by c.id
            """)
    List<UnreadCount> countUnread(@Param("conversationIds") Collection<Long> conversationIds,
                                  @Param("userId") Long userId);

    @Query("select max(m.id) from ChatMessage m where m.conversation.id = :conversationId")
    Optional<Long> findLastMessageId(@Param("conversationId") Long conversationId);
}
